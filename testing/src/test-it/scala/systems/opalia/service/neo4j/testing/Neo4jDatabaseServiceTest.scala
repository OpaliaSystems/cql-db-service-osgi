package systems.opalia.service.neo4j.testing

import com.typesafe.config._
import java.nio.file.{Files, Paths}
import org.scalatest._
import play.api.libs.json.Json
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import systems.opalia.bootloader.ArtifactNameBuilder._
import systems.opalia.bootloader.{Bootloader, BootloaderBuilder}
import systems.opalia.commons.database.converter.DefaultConverter._
import systems.opalia.commons.database.converter.NativeTypesConverter._
import systems.opalia.commons.io.FileUtils
import systems.opalia.commons.json.JsonAstTransformer
import systems.opalia.interfaces.database._
import systems.opalia.interfaces.soa.osgi.ServiceManager


class Neo4jDatabaseServiceTest
  extends FlatSpec
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Matchers {

  val testName = "neo4j-database-service-test"
  val serviceManager = new ServiceManager()

  var bootloader: Bootloader = _
  var databaseService: DatabaseService = _
  var transactional: Transactional = _

  override final def beforeAll(): Unit = {

    val testPath = Paths.get("./tmp").resolve(testName)

    if (Files.exists(testPath))
      FileUtils.deleteRecursively(testPath)

    val config =
      ConfigFactory.load(
        s"$testName.conf",
        ConfigParseOptions.defaults(),
        ConfigResolveOptions.defaults().setAllowUnresolved(true)
      )
        .resolveWith(ConfigFactory.parseString(
          s"""
             |base-path = $testPath
           """.stripMargin))

    bootloader =
      BootloaderBuilder.newBootloaderBuilder(config)
        .withCacheDirectory(testPath.resolve("felix-cache").normalize())
        .withBundle("systems.opalia" %% "logging-impl-logback" % "1.0.0")
        .withBundle("systems.opalia" %% "vfs-backend-api" % "1.0.0")
        .withBundle("systems.opalia" %% "vfs-backend-impl-apachevfs" % "1.0.0")
        .withBundle("systems.opalia" %% "vfs-impl-frontend" % "1.0.0")
        .withBundle("systems.opalia" % "neo4j-api" % "1.0.0")
        .withBundle("systems.opalia" % "neo4j-impl-embedded" % "1.0.0")
        .withBundle("systems.opalia" %% "database-impl-neo4j" % "1.0.0")
        .withBundle("org.osgi" % "org.osgi.util.tracker" % "1.5.2")
        .withBundle("org.osgi" % "org.osgi.util.promise" % "1.1.1")
        .withBundle("org.osgi" % "org.osgi.util.function" % "1.1.0")
        .withBundle("org.osgi" % "org.osgi.util.pushstream" % "1.0.1")
        .withBundle("org.osgi" % "org.osgi.service.component" % "1.4.0")
        .withBundle("org.apache.felix" % "org.apache.felix.scr" % "2.1.16")
        .newBootloader()

    bootloader.setup()

    Await.result(bootloader.awaitUp(), Duration.Inf)

    databaseService = serviceManager.getService(bootloader.bundleContext, classOf[DatabaseService])
    transactional = databaseService.newTransactional()
  }

  override final def afterAll(): Unit = {

    serviceManager.unregisterServices()
    serviceManager.ungetServices(bootloader.bundleContext)

    bootloader.shutdown()

    Await.result(bootloader.awaitDown(), Duration.Inf)
  }

  override def beforeEach(): Unit = {

    transactional.withTransaction {
      implicit executor =>

        Query(
          """
            |create
            |  (Armin:Group1 { name: 'Armin', counter: 25 }),
            |  (Folker:Group1 { name: 'Folker', counter: 28 }),
            |  (Lorelei:Group1 { name: 'Lorelei', counter: 88 }),
            |  (Berthold:Group1:Group2 { name: 'Berthold', counter: 3 }),
            |  (Fritz:Group2 { name: 'Fritz', counter: 42 }),
            |  (Dagmar:Group2 { name: 'Dagmar', counter: 73 }),
            |  (Emma:Group2 { name: 'Emma', counter: 25 }),
            |  (Erhard { name: 'Erhard', counter: 109 }),
            |  (Armin)-[:KNOWS { friend: true }]->(Folker),
            |  (Armin)-[:KNOWS { friend: true }]->(Lorelei),
            |  (Armin)-[:KNOWS { friend: false }]->(Berthold),
            |  (Armin)-[:LOVES]->(Lorelei),
            |  (Lorelei)-[:KNOWS { friend: true }]->(Berthold),
            |  (Lorelei)-[:LOVES]->(Berthold),
            |  (Folker)-[:KNOWS { friend: true }]->(Lorelei),
            |  (Folker)-[:KNOWS { friend: false }]->(Fritz),
            |  (Berthold)-[:KNOWS { friend: true }]->(Dagmar),
            |  (Fritz)-[:KNOWS { friend: true }]->(Emma),
            |  (Dagmar)-[:KNOWS { friend: true }]->(Emma),
            |  (Emma)-[:KNOWS { friend: true }]->(Erhard)
          """.stripMargin)
          .execute[IgnoredResult]()
    }
  }

  override def afterEach(): Unit = {

    transactional.withTransaction {
      implicit executor =>

        Query(
          """
            |match (n)
            |detach delete n
          """.stripMargin)
          .execute[IgnoredResult]()
    }
  }

  it should "be able to fetch the labels from a graph" in {

    transactional.withTransaction {
      implicit executor =>

        val result =
          Query(
            """
              |match (n)
              |return distinct labels(n) as labels
            """.stripMargin)
            .execute[IndexedSeqResult]()
            .transform(row => row[List[String]]("labels"))

        result.toSet should be(Set(List("Group1"), List("Group2"), List("Group1", "Group2"), List()))
    }
  }

  it should "be able to make a query with multiple arguments" in {

    transactional.withTransaction {
      implicit executor =>

        val result =
          Query(
            """
              |match (n:Group1)-[:KNOWS]->(m:Group1)
              |where n.name = {name_1} or n.name = {name_2}
              |return m.name as name
            """.stripMargin)
            .on("name_1", "Folker")
            .on("name_2", "Lorelei")
            .execute[IndexedSeqResult]()
            .transform(row => row[String]("name"))

        result.toSet should be(Set("Lorelei", "Berthold"))
    }
  }

  it should "be able to make a query with a property list as parameters" in {

    transactional.withTransaction {
      implicit executor =>

        val result =
          Query(
            """
              |match (n:Group1)
              |where n.name in {names}
              |return n.name as name
            """.stripMargin)
            .on("names", List("Armin", "Folker", "Fritz"))
            .execute[IndexedSeqResult]()
            .transform(row => row[String]("name"))

        result.toSet should be(Set("Armin", "Folker"))
    }
  }

  it should "be able to convert result rows to JSON" in {

    transactional.withTransaction {
      implicit executor =>

        val result1 =
          Query(
            """
              |match (n:Group1)-[r:LOVES]-(m)
              |where n.name = {name}
              |return m
            """.stripMargin)
            .on("name", "Armin")
            .execute[SingleResult]()
            .transform(row => row.toJson)

        val result2 =
          Query(
            """
              |match (n:Group1)-[:LOVES]-(m:Group2)-[r:KNOWS]-(n:Group1)
              |return r
            """.stripMargin)
            .execute[SingleResult]()
            .transform(row => row.toJson)

        val result3 =
          Query(
            """
              |match p = (n:Group1)-[r:LOVES*]-(m:Group2)
              |return p
            """.stripMargin)
            .execute[IndexedNonEmptySeqResult]()
            .transform(row => row.toJson)

        JsonAstTransformer.toPlayJson(result1) should be(
          Json.obj("m" -> Json.obj("name" -> "Lorelei", "counter" -> 88))
        )

        JsonAstTransformer.toPlayJson(result2) should be(
          Json.obj("r" -> Json.obj("friend" -> true))
        )

        result3.map(JsonAstTransformer.toPlayJson).toSet should be(Set(
          Json.obj("p" -> Json.arr(
            Json.obj("name" -> "Lorelei", "counter" -> 88),
            Json.obj(),
            Json.obj("name" -> "Berthold", "counter" -> 3))),
          Json.obj("p" -> Json.arr(
            Json.obj("name" -> "Armin", "counter" -> 25),
            Json.obj(),
            Json.obj("name" -> "Lorelei", "counter" -> 88),
            Json.obj(),
            Json.obj("name" -> "Berthold", "counter" -> 3)))
        ))
    }
  }

  it should "handle optional parameters" in {

    transactional.withTransaction {
      implicit executor =>

        val result1 =
          Query(
            """
              |match (n:Group2)
              |where n.name = {name}
              |return n.name as name, n.counter as counter
            """.stripMargin)
            .on("name", "Dagmar")
            .execute[SingleOptResult]()

        val result2 =
          Query(
            """
              |match (n:Group2)
              |where n.name = {name}
              |return n.name as name, n.counter as counter
            """.stripMargin)
            .on("name", Some("Dagmar"))
            .execute[SingleOptResult]()

        val result3 =
          Query(
            """
              |match (n:Group2)
              |where n.name = {name}
              |return n.name as name, n.counter as counter
            """.stripMargin)
            .on("name", None)
            .execute[SingleOptResult]()

        val result4 =
          Query(
            """
              |match (n:Group2)
              |where n.name = {name}
              |return n.name as name, n.bla as bla
            """.stripMargin)
            .on("name", "Dagmar")
            .execute[SingleOptResult]()

        val transformer1 =
          (row: Row) => (row[String]("name"), row[Option[Long]]("counter"))

        val transformer2 =
          (row: Row) => (row[String]("name"), row[Option[String]]("counter"))

        val transformer3 =
          (row: Row) => (row[String]("name"), row[Option[String]]("bla"))

        val transformer4 =
          (row: Row) => (row[String]("name"), row[String]("bla"))

        result1.transform(transformer1) shouldBe Some("Dagmar", Some(73))
        result1.transform(transformer2) shouldBe Some("Dagmar", None)

        result2.transform(transformer1) shouldBe Some("Dagmar", Some(73))
        result2.transform(transformer2) shouldBe Some("Dagmar", None)

        result3.transform(transformer1) shouldBe None
        result3.transform(transformer2) shouldBe None

        result4.transform(transformer3) shouldBe Some("Dagmar", None)

        an[IllegalArgumentException] should be thrownBy result4.transform(transformer4)
    }
  }

  it should "handle several collection classes" in {

    transactional.withTransaction {
      implicit executor =>

        val collections1 =
          (Seq(0, 1, 2, 3), List(4, 5, 6, 7), Stream(8, 9, 10, 11), Vector(12, 13, 14, 15), Array(16, 17, 18, 19))

        Query(
          """
            |match (n:Group1)
            |where n.name = 'Lorelei'
            |set n.seq_1 = {seq_1}
            |set n.seq_2 = {seq_2}
            |set n.seq_3 = {seq_3}
            |set n.seq_4 = {seq_4}
            |set n.seq_5 = {seq_5}
          """.stripMargin)
          .on("seq_1", collections1._1)
          .on("seq_2", collections1._2)
          .on("seq_3", collections1._3)
          .on("seq_4", collections1._4)
          .on("seq_5", collections1._5)
          .execute[IgnoredResult]()

        val collections2 =
          Query(
            """
              |match (n:Group1)
              |where n.name ='Lorelei'
              |return n.seq_1, n.seq_2, n.seq_3, n.seq_4, n.seq_5
            """.stripMargin)
            .execute[SingleResult]()
            .transform {
              row =>

                (row[Seq[Int]]("n.seq_5"),
                  row[List[Int]]("n.seq_4"),
                  row[Stream[Int]]("n.seq_3"),
                  row[Vector[Int]]("n.seq_2"),
                  row[Array[Int]]("n.seq_1"))
            }

        collections2._1.isInstanceOf[Seq[_]] should be(true)
        collections2._2.isInstanceOf[List[_]] should be(true)
        collections2._3.isInstanceOf[Stream[_]] should be(true)
        collections2._4.isInstanceOf[Vector[_]] should be(true)
        collections2._5.isInstanceOf[Array[_]] should be(true)

        collections2._1 should be(collections1._5)
        collections2._2 should be(collections1._4)
        collections2._3 should be(collections1._3)
        collections2._4 should be(collections1._2)
        collections2._5 should be(collections1._1)
    }
  }

  it should "be able to parse a required number of rows" in {

    transactional.withTransaction {
      implicit executor =>

        val queryExpectEmpty =
          Query(
            """
              |match (n:Group1)
              |where n.name = {name}
              |return n.name as name
            """.stripMargin)
            .on("name", "Adam")

        val queryExpectOne =
          Query(
            """
              |match (n:Group1)
              |where n.name = {name}
              |return n.name as name
            """.stripMargin)
            .on("name", "Armin")

        val queryExpectMultiple =
          Query(
            """
              |match (n:Group1)
              |return n.name as name
            """.stripMargin)

        val transformer =
          (row: Row) => row[String]("name")

        // single

        an[IllegalArgumentException] should be thrownBy
          queryExpectEmpty.execute[SingleResult]().transform(transformer)

        queryExpectOne.execute[SingleResult]().transform(transformer) shouldBe a[String]

        an[IllegalArgumentException] should be thrownBy
          queryExpectMultiple.execute[SingleResult]().transform(transformer)

        // single optional

        queryExpectEmpty.execute[SingleOptResult]().transform(transformer) shouldBe empty

        queryExpectOne.execute[SingleOptResult]().transform(transformer) shouldBe defined

        an[IllegalArgumentException] should be thrownBy
          queryExpectMultiple.execute[SingleOptResult]().transform(transformer)

        // sequence

        queryExpectEmpty.execute[IndexedSeqResult]().transform(transformer) should have size 0

        queryExpectOne.execute[IndexedSeqResult]().transform(transformer) should have size 1

        queryExpectMultiple.execute[IndexedSeqResult]().transform(transformer) should have size 4

        // non empty sequence

        an[IllegalArgumentException] should be thrownBy
          queryExpectEmpty.execute[IndexedNonEmptySeqResult]().transform(transformer)

        queryExpectOne.execute[IndexedNonEmptySeqResult]().transform(transformer) should have size 1

        queryExpectMultiple.execute[IndexedNonEmptySeqResult]().transform(transformer) should have size 4
    }
  }

  it should "be able to convert native types" in {

    transactional.withTransaction {
      implicit executor =>

        val boolean: Boolean = true
        val byte: Byte = 42
        val short: Short = 1042
        val int: Int = 10042
        val long: Long = 100042
        val float: Float = 0.73f
        val double: Double = 0.073d
        val char: Char = 'Ä'
        val string: String = "test"

        Query(
          """
            |match (n:Group1)
            |where n.name = 'Armin'
            |set n.boolean = {boolean}
            |set n.byte = {byte}
            |set n.short = {short}
            |set n.int = {int}
            |set n.long = {long}
            |set n.float = {float}
            |set n.double = {double}
            |set n.char = {char}
            |set n.string = {string}
          """.stripMargin)
          .on("boolean", boolean)
          .on("byte", byte)
          .on("short", short)
          .on("int", int)
          .on("long", long)
          .on("float", float)
          .on("double", double)
          .on("char", char)
          .on("string", string)
          .execute[IgnoredResult]()

        val result =
          Query(
            """
              |match (n:Group1)
              |where n.name = 'Armin'
              |return n.boolean, n.byte, n.short, n.int, n.long, n.float, n.double, n.char, n.string
            """.stripMargin)
            .execute[SingleResult]()
            .transform {
              row =>

                (row[Boolean]("n.boolean"),
                  row[Byte]("n.byte"),
                  row[Short]("n.short"),
                  row[Int]("n.int"),
                  row[Long]("n.long"),
                  row[Float]("n.float"),
                  row[Double]("n.double"),
                  row[Char]("n.char"),
                  row[String]("n.string"))
            }

        result._1 should be(boolean)
        result._2 should be(byte)
        result._3 should be(short)
        result._4 should be(int)
        result._5 should be(long)
        result._6 should be(float)
        result._7 should be(double)
        result._8 should be(char)
        result._9 should be(string)
    }
  }

  it should "be able to performed online backups" in {

    databaseService.backup()
  }
}

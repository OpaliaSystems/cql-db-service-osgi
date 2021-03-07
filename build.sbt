
val mScalaVersion = "2.12.13"
val mInterfacesVersion = "1.0.0"
val mCommonsVersion = "1.0.0"
val mBootloaderVersion = "1.0.0"
val mCrossVersion = """^(\d+\.\d+)""".r.findFirstIn(mScalaVersion).get

val exclusionRules = Seq(
  ExclusionRule("org.scala-lang", "scala-library"),
  ExclusionRule("org.scala-lang", "scala-reflect"),
  ExclusionRule("org.scala-lang", "scala-compiler"),
  ExclusionRule("com.typesafe", "config"),
  ExclusionRule("systems.opalia", s"interfaces_$mCrossVersion"),
  ExclusionRule("org.osgi", "org.osgi.core"),
  ExclusionRule("org.osgi", "org.osgi.service.component"),
  ExclusionRule("org.osgi", "org.osgi.compendium")
)

def commonSettings: Seq[Setting[_]] = {

  Seq(
    organizationName := "Opalia Systems",
    organizationHomepage := Some(url("https://opalia.systems")),
    organization := "systems.opalia",
    homepage := Some(url("https://github.com/OpaliaSystems/opalia-service-neo4j")),
    version := "1.0.0"
  )
}

lazy val `testing` =
  (project in file("testing"))
    .settings(

      name := "testing",

      scalaVersion := mScalaVersion,

      commonSettings,

      libraryDependencies ++= Seq(
        "systems.opalia" %% "interfaces" % mInterfacesVersion,
        "systems.opalia" %% "commons" % mCommonsVersion,
        "systems.opalia" %% "bootloader" % mBootloaderVersion,
        "org.scalatest" %% "scalatest" % "3.2.5" % "test"
      )
    )

lazy val `neo4j-api` =
  (project in file("neo4j-api"))
    .settings(

      name := "neo4j-api",

      description := "The project provides an API for creating an embedded Neo4j instance.",

      crossPaths := false, // drop off Scala suffix from artifact names

      commonSettings,

      bundleSettings,

      OsgiKeys.importPackage ++= Seq(
      ),

      OsgiKeys.exportPackage ++= Seq(
        "systems.opalia.service.neo4j.embedded.api.*"
      ),

      libraryDependencies ++= Seq(
      )
    )

lazy val `neo4j-impl-embedded` =
  (project in file("neo4j-impl-embedded"))
    .dependsOn(`neo4j-api`)
    .settings(

      name := "neo4j-impl-embedded",

      description := "The project provides an implementation for creating an embedded Neo4j instance.",

      scalaVersion := "2.11.12",

      crossPaths := false, // drop off Scala suffix from artifact names

      commonSettings,

      bundleSettings,

      OsgiKeys.privatePackage ++= Seq(
        "systems.opalia.service.neo4j.embedded.impl.*"
      ),

      OsgiKeys.importPackage ++= Seq(
        "systems.opalia.service.neo4j.embedded.api.*"
      ),

      libraryDependencies ++= Seq(
        "org.osgi" % "osgi.core" % "8.0.0" % "provided",
        "org.osgi" % "org.osgi.service.component.annotations" % "1.4.0",
        "org.osgi" % "org.osgi.service.log" % "1.5.0",
        "org.apache.commons" % "commons-text" % "1.9",
        "org.neo4j" % "neo4j-enterprise" % "3.3.0"
      )
    )

lazy val `database-impl-neo4j` =
  (project in file("database-impl-neo4j"))
    .dependsOn(`neo4j-api`)
    .settings(

      name := "database-impl-neo4j",

      description := "The project provides an implementation for accessing an embedded Neo4j instance.",

      scalaVersion := mScalaVersion,

      commonSettings,

      bundleSettings,

      OsgiKeys.privatePackage ++= Seq(
        "systems.opalia.service.neo4j.impl.*"
      ),

      OsgiKeys.importPackage ++= Seq(
        "scala.*",
        "com.typesafe.config.*",
        "systems.opalia.interfaces.*",
        "systems.opalia.service.neo4j.embedded.api.*"
      ),

      libraryDependencies ++= Seq(
        "org.osgi" % "osgi.core" % "8.0.0" % "provided",
        "org.osgi" % "org.osgi.service.component.annotations" % "1.4.0",
        "systems.opalia" %% "interfaces" % mInterfacesVersion % "provided",
        "systems.opalia" %% "commons" % mCommonsVersion excludeAll (exclusionRules: _*)
      )
    )

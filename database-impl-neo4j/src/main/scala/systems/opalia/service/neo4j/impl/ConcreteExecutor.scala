package systems.opalia.service.neo4j.impl

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap
import scala.reflect._
import systems.opalia.interfaces.database._
import systems.opalia.interfaces.json.JsonAst
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseService


class ConcreteExecutor(service: EmbeddedDatabaseService)
  extends Executor {

  def execute[R <: Result : ClassTag](clause: String, parameters: Map[String, Any]): R = {

    if (!parameters.forall(_._1.isInstanceOf[String]))
      throw new IllegalArgumentException(
        s"Expect keys with type ${classOf[String].getName}.")

    val table =
      service.execute(
        clause,
        parameters.map(x => x._1 -> normalizeIn(x._2).asInstanceOf[AnyRef]).asJava
      )

    val statistics =
      JsonAst.JsonObject(ListMap(
        "nodes_created" -> JsonAst.JsonNumberInt(table.getStatistics.getNodesCreated),
        "nodes_deleted" -> JsonAst.JsonNumberInt(table.getStatistics.getNodesDeleted),
        "relationships_created" -> JsonAst.JsonNumberInt(table.getStatistics.getRelationshipsCreated),
        "relationships_deleted" -> JsonAst.JsonNumberInt(table.getStatistics.getRelationshipsDeleted),
        "properties_set" -> JsonAst.JsonNumberInt(table.getStatistics.getPropertiesSet),
        "labels_added" -> JsonAst.JsonNumberInt(table.getStatistics.getLabelsAdded),
        "labels_removed" -> JsonAst.JsonNumberInt(table.getStatistics.getLabelsRemoved),
        "indexes_added" -> JsonAst.JsonNumberInt(table.getStatistics.getIndexesAdded),
        "indexes_removed" -> JsonAst.JsonNumberInt(table.getStatistics.getIndexesRemoved),
        "constraints_added" -> JsonAst.JsonNumberInt(table.getStatistics.getConstraintsAdded),
        "constraints_removed" -> JsonAst.JsonNumberInt(table.getStatistics.getConstraintsRemoved),
        "contains_updates" -> JsonAst.JsonBoolean(table.getStatistics.containsUpdates)
      ))

    val concreteResult =
      if (classTag[R] == classTag[IgnoredResult]) {

        new IgnoredResult {
        }

      } else {

        val rows = table.getRows.asScala.toVector.map(x => toScalaMap(x.getData))
        val columnNames = table.getColumns.asScala.toVector

        if (classTag[R] == classTag[SingleResult]) {

          if (rows.length != 1)
            throw new IllegalArgumentException(
              s"Expect set of rows with cardinality of 1 but ${rows.length} received.")

          new SingleResult {

            def columns: IndexedSeq[String] =
              columnNames

            def meta: JsonAst.JsonObject =
              statistics

            def transform[T](f: Row => T): T =
              rows.map(new ConcreteRow(_)).map(f).head
          }

        } else if (classTag[R] == classTag[SingleOptResult]) {

          if (rows.length > 1)
            throw new IllegalArgumentException(
              s"Expect set of rows with cardinality of 0 or 1 but ${rows.length} received.")

          new SingleOptResult {

            def columns: IndexedSeq[String] =
              columnNames

            def meta: JsonAst.JsonObject =
              statistics

            def transform[T](f: Row => T): Option[T] =
              rows.map(new ConcreteRow(_)).map(f).headOption
          }

        } else if (classTag[R] == classTag[IndexedSeqResult]) {

          new IndexedSeqResult {

            def columns: IndexedSeq[String] =
              columnNames

            def meta: JsonAst.JsonObject =
              statistics

            def transform[T](f: Row => T): IndexedSeq[T] =
              rows.map(new ConcreteRow(_)).map(f)
          }

        } else if (classTag[R] == classTag[IndexedNonEmptySeqResult]) {

          if (rows.isEmpty)
            throw new IllegalArgumentException(
              s"Expect set of rows with cardinality greater than 1 but ${rows.length} received.")

          new IndexedNonEmptySeqResult {

            def columns: IndexedSeq[String] =
              columnNames

            def meta: JsonAst.JsonObject =
              statistics

            def transform[T](f: Row => T): IndexedSeq[T] =
              rows.map(new ConcreteRow(_)).map(f)
          }

        } else
          throw new IllegalArgumentException(
            "Unsupported type of result class.")
      }

    concreteResult.asInstanceOf[R]
  }

  private def normalizeIn(value: Any): Any = {
    value match {

      case null => null
      case x: Boolean => x
      case x: Byte => x
      case x: Short => x
      case x: Integer => x
      case x: Long => x
      case x: Float => x
      case x: Double => x
      case x: Char => x
      case x: String => x
      case x: Map[_, _] => toJavaMap(x)
      case x: Seq[_] => x.map(normalizeIn).asJava

      case _ =>
        throw new IllegalArgumentException(
          s"Cannot normalize value $value (${value.getClass.getName}).")
    }
  }

  private def normalizeOut(value: Any): Any = {
    value match {

      case null => null
      case x: Boolean => x
      case x: Byte => x
      case x: Short => x
      case x: Integer => x
      case x: Long => x
      case x: Float => x
      case x: Double => x
      case x: Char => x
      case x: String => x
      case x: java.util.Map[_, _] => toScalaMap(x)
      case x: java.util.Collection[_] => x.asScala.toSeq.map(normalizeOut)

      case _ =>
        throw new IllegalArgumentException(
          s"Cannot normalize value $value (${value.getClass.getName}).")
    }
  }

  private def toJavaMap(map: Map[_, _]): java.util.LinkedHashMap[String, Any] = {

    val jmap = new java.util.LinkedHashMap[String, Any]

    map.foreach {
      x =>

        jmap.put(x._1.toString, x._2)
    }

    jmap
  }

  private def toScalaMap(map: java.util.Map[_, _]): ListMap[String, Any] = {
    ListMap(map.entrySet().iterator().asScala.toSeq
      .map(x => (x.getKey.toString, normalizeOut(x.getValue))): _*)
  }
}

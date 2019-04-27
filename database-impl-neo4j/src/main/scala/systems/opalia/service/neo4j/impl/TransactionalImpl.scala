package systems.opalia.service.neo4j.impl

import java.time.Instant
import systems.opalia.interfaces.database._
import systems.opalia.interfaces.logging.Logger
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabase


final class TransactionalImpl(database: EmbeddedDatabase,
                              logger: Logger,
                              loggerStats: Logger)
  extends Transactional {

  def withTransaction[T](block: (Executor) => T): T = {

    val start = Instant.now.toEpochMilli

    val result =
      database.withTransaction {
        service =>

          block(new ConcreteExecutor(service))
      }

    val end = Instant.now.toEpochMilli

    loggerStats.debug(s"A transaction was performed in ${end - start} ms.")

    result
  }
}

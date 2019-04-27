package systems.opalia.service.neo4j.impl

import systems.opalia.interfaces.logging.LoggingService
import systems.opalia.service.neo4j.embedded.api.{EmbeddedDatabaseLogger, EmbeddedDatabaseLoggingProvider}


class EmbeddedDatabaseLoggingProviderImpl(loggingService: LoggingService)
  extends EmbeddedDatabaseLoggingProvider {

  def getLogger(name: String): EmbeddedDatabaseLogger =
    new EmbeddedDatabaseLoggerImpl(loggingService.newLogger(name))
}

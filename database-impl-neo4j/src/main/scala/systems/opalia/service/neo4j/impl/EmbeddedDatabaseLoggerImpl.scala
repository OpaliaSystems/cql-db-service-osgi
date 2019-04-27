package systems.opalia.service.neo4j.impl

import systems.opalia.interfaces.logging.Logger
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseLogger


class EmbeddedDatabaseLoggerImpl(logger: Logger)
  extends EmbeddedDatabaseLogger {

  def isDebugEnabled: Boolean =
    logger.debugEnabled

  def isInfoEnabled: Boolean =
    logger.infoEnabled

  def isWarningEnabled: Boolean =
    logger.warningEnabled

  def isErrorEnabled: Boolean =
    logger.errorEnabled

  def debug(message: String): Unit =
    logger.debug(message)

  def debug(message: String, throwable: Throwable): Unit =
    logger.debug(message, throwable)

  def info(message: String): Unit =
    logger.info(message)

  def info(message: String, throwable: Throwable): Unit =
    logger.info(message, throwable)

  def warning(message: String): Unit =
    logger.warning(message)

  def warning(message: String, throwable: Throwable): Unit =
    logger.warning(message, throwable)

  def error(message: String): Unit =
    logger.error(message)

  def error(message: String, throwable: Throwable): Unit =
    logger.error(message, throwable)
}

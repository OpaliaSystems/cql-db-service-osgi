package systems.opalia.service.neo4j.impl

import scala.language.postfixOps
import systems.opalia.commons.io.FileUtils
import systems.opalia.interfaces.database._
import systems.opalia.interfaces.logging.LoggingService
import systems.opalia.interfaces.rendering.Renderer
import systems.opalia.interfaces.soa.Bootable
import systems.opalia.interfaces.vfs.VfsService
import systems.opalia.service.neo4j.embedded.api.{EmbeddedDatabase, EmbeddedDatabaseFactory}


final class DatabaseServiceBootable(config: BundleConfig,
                                    factory: EmbeddedDatabaseFactory,
                                    loggingService: LoggingService,
                                    vfsService: VfsService)
  extends DatabaseService
    with Bootable[Unit, Unit] {

  private val database = configDatabase(config, factory, loggingService)

  private val logger = loggingService.newLogger(classOf[DatabaseService].getName)
  private val loggerStats = loggingService.newLogger(s"${classOf[DatabaseService].getName}-statistics")

  private val backupManager: BackupManager = new BackupManager(config, factory, logger, loggerStats, vfsService)

  def newTransactional(): Transactional =
    new TransactionalImpl(database, logger, loggerStats)

  def backup(): Unit =
    backupManager.backup()

  protected def setupTask(): Unit = {

    database.waitAvailable(Long.MaxValue)
  }

  protected def shutdownTask(): Unit = {

    backupManager.shutdown()
    database.shutdown()
  }

  private def configDatabase(config: BundleConfig,
                             factory: EmbeddedDatabaseFactory,
                             loggingService: LoggingService): EmbeddedDatabase = {

    val dbConfigFile = config.deploymentPath.resolve("database.conf")

    FileUtils.using(new java.io.PrintWriter(dbConfigFile.toFile, Renderer.appDefaultCharset.toString)) {
      writer =>

        if (config.HighlyAvailable.enabled) {

          writer.println(s"ha.server_id=${config.HighlyAvailable.serverId}")
          writer.println(s"ha.slave_only=${config.HighlyAvailable.slaveOnly}")
          writer.println(s"ha.pull_interval=${config.HighlyAvailable.pullInterval.toSeconds}")
          writer.println(s"ha.tx_push_factor=${config.HighlyAvailable.txPushFactor}")
          writer.println(s"ha.host.data=${config.HighlyAvailable.dataServer}")
          writer.println(s"ha.host.coordination=${config.HighlyAvailable.coordinationServer}")
          writer.println(s"ha.initial_hosts=${config.HighlyAvailable.initialNodes.mkString(",")}")
        }

        if (config.Backup.enabled) {

          writer.println(s"dbms.backup.enabled=${true}")
          writer.println(s"dbms.backup.address=${config.Backup.server}")

        } else {

          writer.println(s"dbms.backup.enabled=${false}")
        }
    }

    factory
      .newEmbeddedDatabase(
        dbConfigFile,
        config.graphPath,
        new EmbeddedDatabaseLoggingProviderImpl(loggingService),
        config.HighlyAvailable.enabled,
        config.txRetries,
        config.txBackoff.toMillis)
  }
}

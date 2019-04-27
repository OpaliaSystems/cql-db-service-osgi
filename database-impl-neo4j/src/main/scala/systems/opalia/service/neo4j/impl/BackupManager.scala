package systems.opalia.service.neo4j.impl

import java.time.Instant
import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.duration._
import scala.language.postfixOps
import systems.opalia.commons.identifier.ObjectId
import systems.opalia.commons.io.FileUtils
import systems.opalia.interfaces.logging.Logger
import systems.opalia.interfaces.vfs.VfsService
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseFactory


final class BackupManager(config: BundleConfig,
                          factory: EmbeddedDatabaseFactory,
                          logger: Logger,
                          loggerStats: Logger,
                          vfsService: VfsService) {

  private val intervalMinimum = 1 minute

  private val database =
    if (config.Backup.enabled)
      Some(factory.newEmbeddedDatabaseBackup(
        config.Backup.backupPath,
        config.Backup.server.hostString,
        config.Backup.server.port))
    else
      None

  private val fs = vfsService.getFileSystem("backup")

  private val scheduler =
    Executors.newScheduledThreadPool(1)

  if (config.Backup.enabled && config.Backup.schedule) {

    if (config.Backup.interval < intervalMinimum)
      throw new IllegalArgumentException(s"Backup interval should not be less than $intervalMinimum.")

    scheduler.scheduleAtFixedRate(
      () => {

        backup()
      },
      config.Backup.interval.toMillis,
      config.Backup.interval.toMillis,
      TimeUnit.MILLISECONDS)
  }

  def shutdown(): Unit = {

    scheduler.shutdown()
    scheduler.awaitTermination(Long.MaxValue, TimeUnit.MILLISECONDS)
  }

  def backup(): Unit =
    synchronized {

      if (config.Backup.enabled) {

        val start = Instant.now
        val id = new Array[Byte](ObjectId.length)

        database.foreach(_.backup())

        FileUtils.using(fs.create(id, s"$id.zip", "application/zip")) {
          outputStream =>

            FileUtils.zip(config.Backup.backupPath, outputStream)
        }

        fs.commit(id)

        val end = Instant.now

        loggerStats.info(s"A backup was performed in ${end.toEpochMilli - start.toEpochMilli} ms.")
      }
    }
}

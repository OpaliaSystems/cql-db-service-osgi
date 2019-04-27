package systems.opalia.service.neo4j.impl

import org.osgi.framework.BundleContext
import org.osgi.service.component.annotations._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import systems.opalia.interfaces.database.{DatabaseService, Transactional}
import systems.opalia.interfaces.logging.LoggingService
import systems.opalia.interfaces.soa.ConfigurationService
import systems.opalia.interfaces.soa.osgi.ServiceManager
import systems.opalia.interfaces.vfs.VfsService
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseFactory


@Component(service = Array(classOf[DatabaseService]), immediate = true)
class DatabaseServiceImpl
  extends DatabaseService {

  private val serviceManager: ServiceManager = new ServiceManager()
  private var bootable: DatabaseServiceBootable = _

  @Reference
  private var embeddedDatabaseFactory: EmbeddedDatabaseFactory = _

  @Reference
  private var loggingService: LoggingService = _

  @Reference
  private var vfsService: VfsService = _

  @Activate
  def start(bundleContext: BundleContext): Unit = {

    val configurationService = serviceManager.getService(bundleContext, classOf[ConfigurationService])
    val config = new BundleConfig(configurationService.getConfiguration)

    bootable =
      new DatabaseServiceBootable(
        config,
        embeddedDatabaseFactory,
        loggingService,
        vfsService
      )

    bootable.setup()
    Await.result(bootable.awaitUp(), Duration.Inf)
  }

  @Deactivate
  def stop(bundleContext: BundleContext): Unit = {

    bootable.shutdown()
    Await.result(bootable.awaitUp(), Duration.Inf)

    serviceManager.ungetServices(bundleContext)

    bootable = null
  }

  def newTransactional(): Transactional =
    bootable.newTransactional()

  def backup(): Unit =
    bootable.backup()
}

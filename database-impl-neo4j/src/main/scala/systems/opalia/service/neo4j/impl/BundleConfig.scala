package systems.opalia.service.neo4j.impl

import com.typesafe.config.Config
import java.nio.file.Path
import scala.concurrent.duration._
import systems.opalia.commons.configuration.ConfigHelper._
import systems.opalia.commons.configuration.Reader._
import systems.opalia.commons.net.EndpointAddress


final class BundleConfig(config: Config) {

  val deploymentPath: Path = config.as[Path]("database.deployment-path").normalize
  val graphPath: Path = config.as[Path]("database.graph-path").normalize

  val txRetries: Int = config.as[Int]("database.tx-retries")
  val txBackoff: FiniteDuration = config.as[FiniteDuration]("database.tx-backoff")

  if (txRetries < 0)
    throw new IllegalArgumentException("Expect positive number for transaction retries.")

  object HighlyAvailable {

    val enabled: Boolean = config.as[Option[Boolean]]("database.ha.enabled").getOrElse(false)

    lazy val serverId: Int = config.as[Int]("database.ha.server-id")
    lazy val slaveOnly: Boolean = config.as[Boolean]("database.ha.slave-only")
    lazy val pullInterval: FiniteDuration = config.as[FiniteDuration]("database.ha.pull-interval")
    lazy val txPushFactor: Int = config.as[Int]("database.ha.tx-push-factor")
    lazy val dataServer: EndpointAddress = config.as[EndpointAddress]("database.ha.data-server")
    lazy val coordinationServer: EndpointAddress = config.as[EndpointAddress]("database.ha.coordination-server")
    lazy val initialNodes: List[EndpointAddress] = config.as[List[EndpointAddress]]("database.ha.initial-nodes")
  }

  object Backup {

    val enabled: Boolean = config.as[Option[Boolean]]("database.backup.enabled").getOrElse(false)
    val schedule: Boolean = config.as[Option[Boolean]]("database.backup.schedule").getOrElse(false)

    lazy val backupPath: Path = config.as[Path]("database.backup-path").normalize
    lazy val server: EndpointAddress = config.as[EndpointAddress]("database.backup.server")
    lazy val interval: FiniteDuration = config.as[FiniteDuration]("database.backup.interval")
  }

}

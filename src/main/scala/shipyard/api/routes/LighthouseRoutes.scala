package shipyard.api.routes

import cats.Monad
import cats.syntax.all._
import shipyard.api.endpoints.LighthouseEndpoints
import shipyard.domain.ClusterStats
import shipyard.kernel.{ClusterStatsAlgebra, PersistenceAlgebra}

final case class LighthouseRoutes[F[_]: Monad: ClusterStatsAlgebra: PersistenceAlgebra]() extends HasRoutes[F] {
  private val persistenceAlgebra = implicitly[PersistenceAlgebra[F]]
  private val clusterStatsAlgebra = implicitly[ClusterStatsAlgebra[F]]

  override def routes: List[ShipyardRoute[F]] = List(
    LighthouseEndpoints.getClusterStats.serverLogic[F] { _ =>
      for {
        ships <- clusterStatsAlgebra.shipsCount
        shipments <- clusterStatsAlgebra.shipmentCount
        cpu <- clusterStatsAlgebra.usedCPU
        memory <- clusterStatsAlgebra.freeMemory
        minus <- clusterStatsAlgebra.allocatedByShipments
      } yield ClusterStats(
        requestedMemory = memory,
        requestedCpuPercentage = cpu,
        shipCount = ships,
        shipmentsCount = shipments,
        minusPredictedMemory = minus
      ).asRight
    },
    LighthouseEndpoints.getApplicationState.serverLogic[F] { _ =>
      persistenceAlgebra.loadFromDisk.map(_.asRight)
    },
    LighthouseEndpoints.saveApplicationState.serverLogic[F] { _ =>
      persistenceAlgebra.loadFromDisk.flatMap(s => persistenceAlgebra.saveToDisk(s)).map(_.asRight)
    }
  )
}

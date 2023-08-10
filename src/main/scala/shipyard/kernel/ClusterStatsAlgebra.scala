package shipyard.kernel

import cats.syntax.all._
import cats.Monad
import cats.effect.{Ref, Sync}
import shipyard.domain.HardwareResources

final case class ClusterStatsAlgebra[F[_]: Sync: Monad](private val refState: Ref[F, ApplicationRefState]) {
  def shipsCount: F[Int] =
    refState.get.map(_.ships).map(_.length)

  def shipmentCount: F[Int] =
    refState.get.map(_.shipments).map(_.length)

  def usedCPU: F[Double] =
    refState.get.map(_.ships).map { ships =>
      ships.map(_.currentHardwareResourcesUsage.getOrElse(HardwareResources.undefined).cpu)
        .sum
    }

  def freeMemory: F[Int] =
    refState.get.map(_.ships).map { ships =>
      ships.map(_.currentHardwareResourcesUsage.getOrElse(HardwareResources.undefined).memory)
        .sum
    }

  def allocatedByShipments: F[Int] =
    refState.get.map(_.shipments).map { shipments =>
      shipments.filter(_.shipId.isDefined)
        .map(_.hardwareRequirements.getOrElse(HardwareResources.undefined).memory)
        .sum
    }
}

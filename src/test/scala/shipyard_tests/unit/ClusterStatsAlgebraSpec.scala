package shipyard_tests.unit

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import shipyard.domain.{HardwareResources, Ship, Shipment}
import shipyard.kernel.{ApplicationRefState, ClusterStatsAlgebra}

import java.util.UUID

class ClusterStatsAlgebraSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  "A cluster stats algebra" - {
    val shipId = UUID.randomUUID()
    val appState = ApplicationRefState(
      ships = List(
        Ship(
          id = Some(shipId),
          name = "",
          ipAddress = "",
          shipmentCidr = None,
          lighthouse = false,
          currentHardwareResourcesUsage = Some(
            HardwareResources(
              memory = 11580,
              cpu = 0.3525830861599138
            )
          )
        )
      ),
      shipments = List(
        Shipment(
          id = None,
          name = "",
          image = "",
          hardwareRequirements = Some(
            HardwareResources(
              memory = 1024,
              cpu = 5.0
            )
          ),
          shipId = Some(shipId),
          health = None
        )
      )
    )
    "properly calculates the resources" in {
      for {
        _ <- IO.print("saves one ship and reads it")
        ref <- Ref[IO].of(appState)
        clusterStatsAlgebra = ClusterStatsAlgebra.apply[IO](ref)
        // assertions
        _ <- clusterStatsAlgebra.usedCPU.asserting(_ shouldBe 0.3525830861599138)
        _ <- clusterStatsAlgebra.freeMemory.asserting(_ shouldBe 11580)
        _ <- clusterStatsAlgebra.shipsCount.asserting(_ shouldBe 1)
        _ <- clusterStatsAlgebra.shipmentCount.asserting(_ shouldBe 1)
        _ <- clusterStatsAlgebra.allocatedByShipments.asserting(_ shouldBe 1024)
      } yield ()
    }
  }
}

package shipyard_tests.unit

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import shipyard.api.algebras.ShipmentAlgebra
import shipyard.domain.Shipment
import shipyard.kernel.ApplicationRefState

import java.util.UUID

class ShipmentAlgebraSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  "A shipment algebra" - {
    "creates and finds the shipment" in {
      val shipment = Shipment(
        id = None,
        name = "test",
        image = "test:latest",
        hardwareRequirements = None,
        shipId = Some(UUID.randomUUID()),
        health = None
      )

      for {
        _ <- IO.print("saves one shipment and reads it")
        ref <- Ref[IO].of(ApplicationRefState.empty)
        shipmentAlgebra = ShipmentAlgebra.algebraWithRef[IO](ref)
        _ <- shipmentAlgebra.save(shipment)
        listing <- shipmentAlgebra.findAll
        foundShipment <- shipmentAlgebra.findByShipId(shipment.shipId.get).map(_.head)
        // assertions
        _ <- IO(listing).asserting(_.length shouldBe 1)
        _ <- IO(foundShipment).asserting(_ shouldBe shipment.copy(id = foundShipment.id))
      } yield ()
    }
  }
}

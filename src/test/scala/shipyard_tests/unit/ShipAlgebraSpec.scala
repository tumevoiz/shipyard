package shipyard_tests.unit

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.slf4j.Slf4jLogger
import shipyard.api.algebras.{ShipAlgebra, ShipmentAlgebra}
import shipyard.domain.{Ship, Shipment}
import shipyard.kernel.ApplicationRefState

import java.util.UUID

class ShipAlgebraSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  "A ship algebra" - {
    implicit def unsafeLogger[F[_]: Async] = Slf4jLogger.getLogger[F]
    "creates and finds the ship" in {
      val ship = Ship(
        id = None,
        name = "test",
        ipAddress = "127.0.0.1",
        shipmentCidr = None,
        lighthouse = false,
        currentHardwareResourcesUsage = None
      )

      for {
        _ <- IO.print("saves one ship and reads it")
        ref <- Ref[IO].of(ApplicationRefState.empty)
        shipAlgebra = ShipAlgebra.algebraWithRef[IO](ref)
        _ <- shipAlgebra.save(ship).value
        listing <- shipAlgebra.findAll
        foundShip <- shipAlgebra.findByName(ship.name).map(_.head)
        // assertions
        _ <- IO(listing).asserting(_.length shouldBe 1)
        _ <- IO(foundShip).asserting(_ shouldBe ship.copy(id = foundShip.id))
      } yield ()
    }
  }
}

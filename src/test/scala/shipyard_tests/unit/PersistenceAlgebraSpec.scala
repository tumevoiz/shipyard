package shipyard_tests.unit

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import shipyard.domain.Ship
import shipyard.kernel.{ApplicationRefState, PersistenceAlgebra}
import shipyard.runtime.DockerRuntime
import shipyard_tests.mocks.MockPersistenceAlgebra

import java.util.UUID

class PersistenceAlgebraSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  "A persistence algebra" - {
    "saves one shipment and reads it" in {
      val newApplicationState = ApplicationRefState(
        ships = List(
          Ship(
            id = Some(UUID.randomUUID()),
            name = "test",
            ipAddress = "127.0.0.1",
            shipmentCidr = Some("127.0.0.1/32"),
            lighthouse = false,
            currentHardwareResourcesUsage = None
          )
        ),
        shipments = List.empty
      )

      for {
        _ <- IO.print("saves one shipment and reads it")
        ref <- Ref[IO].of(ApplicationRefState.empty)
        persistenceAlgebra: PersistenceAlgebra[IO] = new MockPersistenceAlgebra[IO](ref)
        _ <- persistenceAlgebra.saveToDisk(newApplicationState)
        refState <- persistenceAlgebra.loadFromDisk
        _ <- IO(refState).asserting(_ shouldBe newApplicationState)
      } yield ()
    }

    "saves one shipment and loads it from lighthouse" in {
      val newApplicationState = ApplicationRefState(
        ships = List(
          Ship(
            id = Some(UUID.randomUUID()),
            name = "test",
            ipAddress = "127.0.0.1",
            shipmentCidr = Some("127.0.0.1/32"),
            lighthouse = false,
            currentHardwareResourcesUsage = None
          )
        ),
        shipments = List.empty
      )

      for {
        _ <- IO.print("saves one shipment and reads it from lighthouse")
        ref <- Ref[IO].of(ApplicationRefState.empty)
        persistenceAlgebra: PersistenceAlgebra[IO] = new MockPersistenceAlgebra[IO](ref)
        _ <- persistenceAlgebra.saveToDisk(newApplicationState)
        refState <- persistenceAlgebra.loadFromDisk
        _ <- IO(refState).asserting(_ shouldBe newApplicationState)
      } yield ()
    }
  }
}

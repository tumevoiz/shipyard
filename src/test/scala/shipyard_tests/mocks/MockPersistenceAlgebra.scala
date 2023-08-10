package shipyard_tests.mocks

import cats.data.EitherT
import cats.effect.{Ref, Sync}
import shipyard.ShipyardError
import shipyard.domain.IPAddress
import shipyard.kernel.{ApplicationRefState, PersistenceAlgebra}

class MockPersistenceAlgebra[F[_]: Sync](ref: Ref[F, ApplicationRefState])
    extends PersistenceAlgebra[F] {
  override def saveToDisk(applicationRefState: ApplicationRefState): F[Unit] = {
    ref.update(_ => applicationRefState)
  }

  override def loadFromDisk: F[ApplicationRefState] = ref.get

  override def loadFromLighthouse(
      lighthouseIP: IPAddress,
      shipName: String
  ): EitherT[F, ShipyardError, ApplicationRefState] =
    EitherT.right(ref.get)
}

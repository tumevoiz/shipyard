package shipyard.kernel

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import cats.effect.Async
import fs2.text
import fs2.io.file.{Files, Flags, Path}
import fs2.Stream
import io.circe.fs2.{decoder, stringParser, stringStreamParser}
import io.circe.syntax._
import org.typelevel.log4cats.Logger
import shipyard.{LighthouseError, ShipyardError}
import shipyard.api.endpoints.{LighthouseEndpoints, ShipEndpoints}
import shipyard.domain.IPAddress
import sttp.client3.UriContext
import sttp.tapir.client.sttp.SttpClientInterpreter

trait PersistenceAlgebra[F[_]] {
  def saveToDisk(applicationRefState: ApplicationRefState): F[Unit]
  def loadFromDisk: F[ApplicationRefState]
  def loadFromLighthouse(
      lighthouseIP: IPAddress,
      shipName: String
  ): EitherT[F, ShipyardError, ApplicationRefState]
}

object PersistenceAlgebra {
  implicit def algebra[F[_]: Logger: Monad: Files: Async](fileName: String): PersistenceAlgebra[F] =
    new PersistenceAlgebra[F] {
      private val stateFilePath = Path(
        s"${System.getProperty("user.home")}/.shipyard/$fileName.json"
      )

      override def saveToDisk(applicationRefState: ApplicationRefState): F[Unit] = {
        Monad[F].ifM(Files[F].exists(stateFilePath))(
          Logger[F].debug("State file already exists.") *>
            Monad[F].unit,
          Logger[F].debug("State file doesn't exists") *>
            Files[F].createFile(stateFilePath)
        ) >>
          Stream
            .emit(applicationRefState.asJson.toString())
            .through(text.utf8.encode)
            .through(Files[F].writeAll(stateFilePath, Flags.Write))
            .compile
            .drain
      }

      override def loadFromDisk: F[ApplicationRefState] =
        Monad[F].ifM(Files[F].exists(stateFilePath))(
          Logger[F].debug("State file found. Loading.") *>
            Files[F]
              .readAll(stateFilePath)
              .through(text.utf8.decode)
              .through(stringStreamParser)
              .through(decoder[F, ApplicationRefState])
              .compile
              .foldMonoid,
          Logger[F].debug("State file not found. Creating a new one.") *>
            Monad[F].pure(ApplicationRefState.empty)
        )

      override def loadFromLighthouse(
          lighthouseIP: IPAddress,
          shipName: String
      ): EitherT[F, ShipyardError, ApplicationRefState] = {
        val client = SttpClientInterpreter().toQuickClient(
          LighthouseEndpoints.getApplicationState,
          baseUri = uri"http://${lighthouseIP}:8080".some
        )
        val result = client(shipName)
        EitherT.fromEither(result)
      }
    }

  def apply[F[_]: PersistenceAlgebra]: PersistenceAlgebra[F] = implicitly[PersistenceAlgebra[F]]
}

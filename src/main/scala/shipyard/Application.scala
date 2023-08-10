package shipyard

import cats._
import cats.data.EitherT
import cats.implicits._
import cats.effect.{Async, ExitCode, IO, IOApp, Ref, Spawn, Sync, Temporal}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import shipyard.api.algebras.{ShipAlgebra, ShipmentAlgebra}
import shipyard.api.endpoints.ShipEndpoints
import shipyard.api.routes.{LighthouseRoutes, ShipRoutes, ShipmentRoutes}
import shipyard.domain.{IPAddress, Ship}
import shipyard.kernel.{ApplicationRefState, ClusterStatsAlgebra, PersistenceAlgebra}
import shipyard.kernel.http.{WebServiceAlgebra, WebServiceConfig}
import shipyard.kernel.scheduling.SchedulerAlgebra
import shipyard.runtime.{ContainerRuntime, DockerRuntime}
import sttp.client3.UriContext
import sttp.tapir.client.sttp.SttpClientInterpreter

import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.duration.DurationInt

object Application extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit def unsafeLogger[F[_]: Async] = Slf4jLogger.getLogger[F]

    val config = System.getenv("SHIPYARD_MODE") match {
      case "lighthouse" => lhConfiguration[IO]
      case "ship" => {
        val lhIP = System.getenv("LHIP")
        val shipName = System.getenv("SHIP_NAME")
        val shipIP = System.getenv("SHIP_IP")
        shipConfiguration[IO](lhIP, shipIP, shipName).value.flatMap {
          case Right(c) => c.pure[IO]
        }
      }
    }

    config.flatMap { wsConfig =>
      WebServiceAlgebra[IO](wsConfig).start.useForever
    }
  }

  private def shipConfiguration[F[_]: Async: Monad: Logger: Spawn](lhIP: IPAddress, ownIP: IPAddress, shipName: String): EitherT[F, ShipyardError, WebServiceConfig[F]] = {
    implicit val persistenceAlgebra = PersistenceAlgebra.algebra(s"$shipName.ship")
    registerShipAtLh[F](lhIP, ownIP, shipName)
      .leftSemiflatTap(e => Logger[F].error("Cannot register new ship:" + e.message))
      .leftFlatMap(_ => EitherT.rightT[F, ShipyardError](())) *>
      persistenceAlgebra.loadFromLighthouse(lhIP, shipName).map { s =>
        Ref[F].of(s)
      } flatMap { stateF =>
        val res = stateF.flatMap { stateRef =>
          implicit val containerRuntime = DockerRuntime[F]
          implicit val shipmentAlgebra = ShipmentAlgebra.algebraWithRef(stateRef)
          implicit val schedulerAlgebra = SchedulerAlgebra[F](lhIP, shipName)
          for {
            _ <- Logger[F].info("Running as ship.")
            _ <- Spawn[F].start(schedulerAlgebra.runShipTasks(persistenceAlgebra))
            wsConfig <- Async[F].pure(WebServiceConfig(
              "0.0.0.0",
              8081,
              List.empty
            ))
          } yield wsConfig
        }

        EitherT.right(res)
      }
  }

  private def registerShipAtLh[F[_]: Monad](lighthouseIP: IPAddress, ownIP: IPAddress, shipName: String): EitherT[F, ShipyardError, Unit] = {
    val client = SttpClientInterpreter().toQuickClient(ShipEndpoints.create, baseUri = uri"http://${lighthouseIP}:8080".some)
    val ship = Ship(
      id = None,
      name = shipName,
      ipAddress = ownIP,
      shipmentCidr = None,
      lighthouse = false,
      currentHardwareResourcesUsage = None
    )
    val result = client(ship)
    EitherT.fromEither(result)
  }

  private def lhConfiguration[F[_]: Async: Monad: Logger]: F[WebServiceConfig[F]] = {
    implicit val persistenceAlgebra = PersistenceAlgebra.algebra(s"lighthouse")
    persistenceAlgebra.loadFromDisk.flatMap { s =>
      Ref[F].of(s)
    } flatMap { state =>
      implicit val shipAlgebra = ShipAlgebra.algebraWithRef(state)
      implicit val shipmentAlgebra = ShipmentAlgebra.algebraWithRef(state)
      implicit val clusterStatsAlgebra = ClusterStatsAlgebra.apply(state)

      implicit val containerRuntime = DockerRuntime[F]
      implicit val schedulerAlgebra = SchedulerAlgebra[F]("127.0.0.1", "lighthouse")

      implicit val shipRoutes = ShipRoutes[F]
      implicit val shipmentRoutes = ShipmentRoutes[F]
      implicit val lighthouseRoutes = LighthouseRoutes[F]

      for {
        _ <- Logger[F].info("Running as lighthouse.")
        _ <- Spawn[F].start(schedulerAlgebra.runLhTasks(state))
        wsConfig <- Async[F].pure(WebServiceConfig(
          "0.0.0.0",
          8080,
          lighthouseRoutes.routes ++ shipRoutes.routes ++ shipmentRoutes.routes
        ))
      } yield wsConfig
    }
  }
}

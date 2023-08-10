package shipyard.api.algebras

import cats._
import cats.data._
import cats.effect.Ref
import cats.implicits._
import org.typelevel.log4cats.Logger
import shipyard.{LighthouseError, ResourceNotFoundError, ShipyardError}
import shipyard.domain.{HardwareResources, Ship, ShipId}
import shipyard.kernel.ApplicationRefState

import java.util.UUID

trait ShipAlgebra[F[_]] {
  def findAll: F[List[Ship]]
  def findByUUID(uuid: ShipId): F[Option[Ship]]
  def findByName(name: String): F[Option[Ship]]
  def save(ship: Ship): EitherT[F, ShipyardError, Unit]
  def updateResources(shipId: ShipId, hardwareResources: HardwareResources): F[Unit]
}

object ShipAlgebra {
  implicit def algebraWithRef[F[_]: Logger: Monad](
      applicationState: Ref[F, ApplicationRefState]
  ): ShipAlgebra[F] =
    new ShipAlgebra[F] {
      override def findAll: F[List[Ship]] =
        applicationState.get.map(_.ships)

      override def findByUUID(uuid: ShipId): F[Option[Ship]] =
        applicationState.get.map(_.ships.find(_.id === uuid.some))

      override def findByName(name: String): F[Option[Ship]] =
        applicationState.get.map(_.ships.find(_.name === name))

      override def save(ship: Ship): EitherT[F, ShipyardError, Unit] = {
        for {
          _ <- validateIfNameExists(ship.name)
          shipWithUUID = ship.copy(id = UUID.randomUUID().some)
          _ <- EitherT.right(applicationState.tryUpdate {
            state => state.copy(ships = state.ships.::(shipWithUUID))
          })
        } yield ()
      }

      private def validateIfNameExists(shipName: String): EitherT[F, ShipyardError, Unit] = EitherT {
        findAll.map { ships =>
          ships.find(_.name === shipName)
        } map {
          case Some(_) => Left(LighthouseError("The ship with this name already exists!"))
          case None => Right(())
        }
      }

      override def updateResources(shipId: ShipId, hardwareResources: HardwareResources): F[Unit] = {
        applicationState.update { state =>
          val oldShip = state.ships.find(_.id === shipId.some)
          oldShip match {
            case Some(ship) => {
              val shipsIntersection = state.ships.filterNot(_.id === ship.id)
              Logger[F].info("Ship resources updated.")
              state.copy(ships = shipsIntersection ++ List(ship.copy(currentHardwareResourcesUsage = hardwareResources.some)))
            }
            case None => {
              Logger[F].error("Cannot update resources of ship! No ship found with this ID.")
              state
            }
          }
        }
      }
    }
}

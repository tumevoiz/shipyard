package shipyard.api.algebras

import cats._
import cats.effect.Ref
import cats.implicits._
import shipyard.domain.{ShipId, Shipment, ShipmentId}
import shipyard.kernel.ApplicationRefState

import java.util.UUID

trait ShipmentAlgebra[F[_]] {
  def findAll: F[List[Shipment]]
  def findByUUID(uuid: ShipmentId): F[Option[Shipment]]
  def findByShipId(shipId: ShipId): F[List[Shipment]]
  def save(shipment: Shipment, id: ShipmentId = UUID.randomUUID()): F[Unit]
  def delete(shipmentId: ShipmentId): F[Unit]
}

object ShipmentAlgebra {
  implicit def algebraWithRef[F[_]: Monad](applicationState: Ref[F, ApplicationRefState]): ShipmentAlgebra[F] =
    new ShipmentAlgebra[F] {
      override def findAll: F[List[Shipment]] =
        applicationState.get.map(_.shipments)

      override def findByUUID(uuid: ShipmentId): F[Option[Shipment]] =
        applicationState.get.map(_.shipments.find(_.id === uuid.some))

      override def findByShipId(shipId: ShipId): F[List[Shipment]] =
        applicationState.get.map(_.shipments.filter(_.shipId === shipId.some))

      override def save(shipment: Shipment, id: ShipmentId = UUID.randomUUID()): F[Unit] = {
        val shipmentWithUUID = shipment.copy(id = id.some)
        applicationState.update {
          state => state.copy(shipments = state.shipments.::(shipmentWithUUID))
        }
      }

      override def delete(shipmentId: ShipmentId): F[Unit] = {
        applicationState.update {
          state => state.copy(shipments = state.shipments.filterNot(_.id === shipmentId.some))
        }
      }
    }
}
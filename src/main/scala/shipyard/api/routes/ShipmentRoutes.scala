package shipyard.api.routes

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import shipyard.{ResourceNotFoundError, ShipyardError}
import shipyard.api.algebras.ShipmentAlgebra
import shipyard.api.endpoints.ShipmentEndpoints
import shipyard.kernel.scheduling.SchedulerAlgebra

final case class ShipmentRoutes[F[_]: Monad: ShipmentAlgebra: SchedulerAlgebra]()
    extends HasRoutes[F] {
  private val shipmentAlgebra = implicitly[ShipmentAlgebra[F]]
  private val schedulerAlgebra = implicitly[SchedulerAlgebra[F]]

  def routes: List[ShipyardRoute[F]] = List(
    ShipmentEndpoints.findAll.serverLogic[F](_ => {
      shipmentAlgebra.findAll.map(_.asRight)
    }),
    ShipmentEndpoints.findByShipId.serverLogic[F] { shipId =>
      shipmentAlgebra.findByShipId(shipId).map(_.asRight)
    },
    ShipmentEndpoints.create.serverLogic[F](shipment => {
      shipmentAlgebra.save(shipment).map(_.asRight)
    }),
    ShipmentEndpoints.delete.serverLogic[F](shipmentId => {
      shipmentAlgebra.delete(shipmentId).map(_.asRight)
    }),
    ShipmentEndpoints.setStatus.serverLogic[F] { case (id, health) =>
      (for {
        shipment <- EitherT.fromOptionF(
          shipmentAlgebra.findByUUID(id),
          new ResourceNotFoundError(id.toString, "shipment")
        )
        _ <- EitherT.liftF[F, ShipyardError, Unit](shipmentAlgebra.delete(shipment.id.get))
        _ <- EitherT.liftF[F, ShipyardError, Unit](
          shipmentAlgebra.save(
            shipment.copy(
              health = health.some
            ),
            shipment.id.get
          )
        )
      } yield ()).leftMap(e => ShipyardError.mapToShipyardError(e)).value
    }
  )
}

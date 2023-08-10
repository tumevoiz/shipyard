package shipyard.api.routes

import cats.{ApplicativeError, Monad, MonadError}
import cats.syntax.all._
import shipyard.api.algebras.ShipAlgebra
import shipyard.api.endpoints.ShipEndpoints
import shipyard.{ResourceNotFoundError, ShipyardError}

final case class ShipRoutes[F[_]: Monad: ShipAlgebra]() extends HasRoutes[F] {
  private val shipAlgebra = implicitly[ShipAlgebra[F]]
  def routes: List[ShipyardRoute[F]] = List(
    ShipEndpoints.findAll.serverLogic[F](_ => {
      shipAlgebra.findAll.map(_.asRight)
    }),
    ShipEndpoints.findByUUID.serverLogic[F](shipId => {
      shipAlgebra.findByUUID(shipId).map {
        case Some(ship) => ship.asRight
        case None => ResourceNotFoundError(shipId.toString, "ship").asLeft
      }
    }),
    ShipEndpoints.findByName.serverLogic[F](shipName => {
      shipAlgebra.findByName(shipName).map {
        case Some(ship) => ship.asRight
        case None => ResourceNotFoundError(shipName, "ship").asLeft
      }
    }),
    ShipEndpoints.updateResources.serverLogic[F]{ case (shipId, hardwareResources) =>
      shipAlgebra.updateResources(shipId, hardwareResources).map(_.asRight)
    },
    ShipEndpoints.create.serverLogic[F](ship => {
      shipAlgebra.save(ship).value
    })
  )
}

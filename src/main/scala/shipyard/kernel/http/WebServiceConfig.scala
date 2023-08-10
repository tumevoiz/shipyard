package shipyard.kernel.http

import cats.effect.Async
import shipyard.api.routes.ShipyardRoute

final case class WebServiceConfig[F[_]: Async](host: String, port: Int, routes: List[ShipyardRoute[F]])

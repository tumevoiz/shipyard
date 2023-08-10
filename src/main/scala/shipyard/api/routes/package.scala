package shipyard.api

import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint

package object routes {
  type ShipyardRoute[F[_]] = ServerEndpoint[Fs2Streams[F], F]

  trait HasRoutes[F[_]] {
    def routes: List[ShipyardRoute[F]]
  }
}

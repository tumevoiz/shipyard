package shipyard.kernel.http

import cats.syntax.all._
import cats.effect.{Async, Resource}
import org.http4s.{HttpRoutes, Method}
import org.http4s.ember.server.EmberServerBuilder
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import com.comcast.ip4s.{Host, Port}
import org.http4s.server.Server
import org.http4s.server.middleware.CORS
import org.typelevel.log4cats.Logger
import shipyard.api.routes.ShipyardRoute
import sttp.tapir.server.interceptor.cors.CORSConfig
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.duration.DurationInt

final case class WebServiceAlgebra[F[_]: Async: Logger](config: WebServiceConfig[F]) {
  def start: Resource[F, Server] = {
    EmberServerBuilder
      .default[F]
      .withHost(Host.fromString(config.host).getOrElse(com.comcast.ip4s.Host.fromString("0.0.0.0").get))
      .withPort(Port.fromInt(config.port).getOrElse(com.comcast.ip4s.Port.fromInt(8080).get))
      .withHttpApp(CORS.policy
        .withAllowOriginAll
        .withAllowMethodsIn(Set(Method.GET, Method.POST, Method.PUT, Method.DELETE, Method.OPTIONS))
        .withAllowCredentials(false)
        .withMaxAge(1.day)
        .apply(interpretRoutes(config.routes ++ swaggerRoutes(config.routes))).orNotFound)
      .build
  }

  private def interpretRoutes(
      routesCollection: List[ServerEndpoint[Fs2Streams[F], F]]
  ): HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(routesCollection)

  private def swaggerRoutes(routesCollection: List[ShipyardRoute[F]]) =
    SwaggerInterpreter().fromServerEndpoints(routesCollection, "shipyard", "0.1.0")
}

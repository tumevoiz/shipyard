package shipyard

import shipyard.api.endpoints.{LighthouseEndpoints, ShipEndpoints, ShipmentEndpoints}
import sttp.apispec.openapi.OpenAPI
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.apispec.openapi.circe.yaml._

object GenerateOpenAPI extends App {
  val endpoints = LighthouseEndpoints.endpoints ++ ShipEndpoints.endpoints ++ ShipmentEndpoints.endpoints
  val docs: OpenAPI = OpenAPIDocsInterpreter().toOpenAPI(endpoints, "Shipyard", "1.0.0")
    .addServer("http://localhost:8080")

  import java.io.PrintWriter

  new PrintWriter("shipyard-api.yaml") {
    write(docs.toYaml); close()
  }
}

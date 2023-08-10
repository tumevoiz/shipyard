package shipyard.api

import shipyard.ShipyardError
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

package object endpoints {
  type ShipyardEndpoint[Input, Output] =
    Endpoint[Unit, Input, ShipyardError, Output, Any]

  trait HasEndpointDefinition {
    def endpoints: List[ShipyardEndpoint[_, _]]

    protected def baseEndpoint = endpoint.errorOut(jsonBody[ShipyardError])
  }
}

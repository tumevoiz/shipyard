package shipyard.api.endpoints

import shipyard.domain.{ShipId, Shipment, ShipmentHealth, ShipmentId}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe._

object ShipmentEndpoints extends HasEndpointDefinition {
  def prefix: EndpointInput[Unit] = "api" / "shipments"

  def findAll: ShipyardEndpoint[Unit, List[Shipment]] =
    baseEndpoint.in(prefix).get
      .description("Find all shipments")
      .out(jsonBody[List[Shipment]])

  def findByShipId: ShipyardEndpoint[ShipId, List[Shipment]] =
    baseEndpoint.in(prefix / path[ShipId]("shipId")).get
      .description("Find all shipments by ship ID")
      .out(jsonBody[List[Shipment]])

  def create: ShipyardEndpoint[Shipment, Unit] =
    baseEndpoint.in(prefix).in(jsonBody[Shipment]).post
      .description("Schedule a new shipment")
      .out(statusCode(StatusCode.Ok))

  def delete: ShipyardEndpoint[ShipmentId, Unit] =
    baseEndpoint.in(prefix / path[ShipmentId]("shipmentId")).delete
      .description("Delete shipment by ID")
      .out(statusCode(StatusCode.Ok))

  def setStatus: ShipyardEndpoint[(ShipmentId, ShipmentHealth), Unit] =
    baseEndpoint.in(prefix / path[ShipmentId]("shipmentId") / "health").in(jsonBody[ShipmentHealth])
      .put
      .description("Update the shipment status")
      .out(statusCode(StatusCode.Ok))

  override def endpoints: List[ShipyardEndpoint[_, _]] = List(
    findAll, findByShipId, create, delete, setStatus
  )
}

package shipyard.api.endpoints

import shipyard.domain.{HardwareResources, Ship, ShipId}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe._

object ShipEndpoints extends HasEndpointDefinition {
  def prefix: EndpointInput[Unit] = "api" / "ships"

  def findAll: ShipyardEndpoint[Unit, List[Ship]] =
    baseEndpoint.in(prefix)
      .get
      .out(jsonBody[List[Ship]])

  def findByUUID: ShipyardEndpoint[ShipId, Ship] =
    baseEndpoint.in(prefix / path[ShipId]("shipId"))
      .get
      .out(jsonBody[Ship])

  def findByName: ShipyardEndpoint[String, Ship] =
    baseEndpoint.in(prefix / path[String]("name"))
      .get
      .out(jsonBody[Ship])

  def create: ShipyardEndpoint[Ship, Unit] =
    baseEndpoint.in(prefix)
      .in(jsonBody[Ship])
      .post

  def updateResources: ShipyardEndpoint[(ShipId, HardwareResources), Unit] =
    baseEndpoint.in(prefix / path[ShipId] / "resources")
      .in(jsonBody[HardwareResources])
      .put

  override def endpoints = List(
    findAll, findByUUID, findByName, create, updateResources
  )
}

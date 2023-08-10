package shipyard.api.endpoints

import shipyard.domain.{ClusterStats, Ship}
import shipyard.kernel.ApplicationRefState
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe._

object LighthouseEndpoints extends HasEndpointDefinition {
  private val prefix = "api" / "lighthouse"
  def getClusterStats: ShipyardEndpoint[Unit, ClusterStats] =
    baseEndpoint.in(prefix / "cluster-stats").get.out(jsonBody[ClusterStats])

  def getApplicationState: ShipyardEndpoint[Unit, ApplicationRefState] =
    baseEndpoint.in(prefix / "state").get.out(jsonBody[ApplicationRefState])

  def saveApplicationState: ShipyardEndpoint[Unit, Unit] =
    baseEndpoint.in(prefix / "state").get.out(statusCode(StatusCode.Ok))

  override def endpoints: List[ShipyardEndpoint[_, _]] = List(getClusterStats, getApplicationState, saveApplicationState)
}

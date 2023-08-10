package shipyard.domain

import io.circe._
import io.circe.generic.semiauto._
import sttp.tapir.Schema

final case class ClusterStats(
  requestedMemory: Double,
  requestedCpuPercentage: Double,
  shipCount: Int,
  shipmentsCount: Int,
  minusPredictedMemory: Int
)

object ClusterStats {
  implicit val encoder: Encoder[ClusterStats] = deriveEncoder
  implicit val decoder: Decoder[ClusterStats] = deriveDecoder

  implicit val schema: Schema[ClusterStats] = Schema.derived
}
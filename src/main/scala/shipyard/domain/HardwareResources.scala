package shipyard.domain

import io.circe._
import io.circe.generic.semiauto._
import sttp.tapir.Schema

case class HardwareResources(memory: Int, cpu: Double)

object HardwareResources {
  def undefined: HardwareResources = HardwareResources(-1, -1.0)

  implicit val encoder: Encoder[HardwareResources] = deriveEncoder
  implicit val decoder: Decoder[HardwareResources] = deriveDecoder

  implicit val schema: Schema[HardwareResources] = Schema.derived[HardwareResources]
}

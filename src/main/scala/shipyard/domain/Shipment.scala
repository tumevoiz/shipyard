package shipyard.domain

import cats.syntax.all._
import enumeratum._
import io.circe._
import io.circe.generic.semiauto._
import sttp.tapir.{FieldName, Schema, SchemaType}
import sttp.tapir.codec.enumeratum._

final case class Shipment(
    id: Option[ShipmentId],
    name: String,
    image: String,
    hardwareRequirements: Option[HardwareResources],
    shipId: Option[ShipId],
    health: Option[ShipmentHealth]
)

sealed trait ShipmentHealth extends EnumEntry
object ShipmentHealth extends Enum[ShipmentHealth] with CirceEnum[ShipmentHealth] with TapirCodecEnumeratum {
  case object Unavailable extends ShipmentHealth
  case object Detached extends ShipmentHealth
  case object Running extends ShipmentHealth

  val values = findValues

//  implicit val schema: Schema[ShipmentHealth] = Schema.schemaFor
}

object Shipment {
  implicit val encoder: Encoder[Shipment] = deriveEncoder

  implicit val decoder: Decoder[Shipment] = deriveDecoder

  implicit val schema: Schema[Shipment] = Schema.derived
}
package shipyard.domain

import cats.syntax.all._
import io.circe._
import io.circe.generic.semiauto._
import sttp.tapir.{FieldName, Schema, SchemaType}

final case class Ship(
  id: Option[ShipId],
  name: String,
  ipAddress: IPAddress,
  shipmentCidr: Option[CIDR],
  lighthouse: Boolean,
  currentHardwareResourcesUsage: Option[HardwareResources]
)

object Ship {
  implicit val encoder: Encoder[Ship] = deriveEncoder

  implicit val decoder: Decoder[Ship] = deriveDecoder

  implicit val schema: Schema[Ship] = Schema(
    schemaType = SchemaType.SProduct(List(
      SchemaType.SProductField(FieldName("id"), Schema.schemaForUUID, _.id),
      SchemaType.SProductField(FieldName("name"), Schema.string, _.name.some),
      SchemaType.SProductField(FieldName("ipAddress"), Schema.string, _.ipAddress.some),
      SchemaType.SProductField(FieldName("shipmentCidr"), Schema.string, _.shipmentCidr),
      SchemaType.SProductField(FieldName("lighthouse"), Schema.schemaForBoolean, _.lighthouse.some),
      SchemaType.SProductField(
        FieldName("currentHardwareResourcesUsage"),
        Schema.derived[HardwareResources],
        _.currentHardwareResourcesUsage
      )
    )
    ),
    description = "Dock is representation of computing node".some,
    name = Schema.SName("Ship").some
  )
}

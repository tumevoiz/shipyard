package shipyard

import sttp.tapir.Schema

import java.util.UUID

package object domain {
  type IPAddress = String
  type CIDR = String

  type ShipmentId = UUID
  object ShipmentId {
    implicit val schema: Schema[ShipmentId] = Schema.schemaForUUID
  }

  type ShipId = UUID
  object ShipId {
    implicit val schema: Schema[ShipId] = Schema.schemaForUUID
  }
}

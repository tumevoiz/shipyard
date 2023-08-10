package shipyard.kernel

import cats.Monoid
import io.circe._
import io.circe.generic.semiauto._
import shipyard.domain.{Ship, ShipId, Shipment}
import sttp.tapir.Schema

import java.util.UUID

final case class ApplicationRefState(
  ships: List[Ship],
  shipments: List[Shipment]
)

object ApplicationRefState {
  def empty: ApplicationRefState = ApplicationRefState(List.empty, List.empty)

  implicit val encoder: Encoder[ApplicationRefState] = deriveEncoder
  implicit val decoder: Decoder[ApplicationRefState] = deriveDecoder

  implicit val monoid: Monoid[ApplicationRefState] = new Monoid[ApplicationRefState] {
    override def empty: ApplicationRefState = ApplicationRefState.empty

    override def combine(x: ApplicationRefState, y: ApplicationRefState): ApplicationRefState =
      ApplicationRefState(
        ships = x.ships ++ y.ships,
        shipments = x.shipments ++ y.shipments
      )
  }

  implicit val schema: Schema[ApplicationRefState] = Schema.derived
}
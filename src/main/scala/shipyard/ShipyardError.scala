package shipyard

import cats.syntax.all._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import sttp.tapir._

sealed trait ShipyardError extends Throwable {
  def message: String
  def kind: String
}

object ShipyardError {
  implicit val encoder: Encoder[ShipyardError] = Encoder.instance {
    case r @ ResourceNotFoundError(_, _) => r.asJson
    case le @ LighthouseError(_) => le.asJson
  }

  implicit val decoder: Decoder[ShipyardError] = List[Decoder[ShipyardError]](
    Decoder[ResourceNotFoundError].widen,
    Decoder[LighthouseError].widen,
  ).reduceLeft(_ or _)

  implicit val schema: Schema[ShipyardError] = Schema.oneOfUsingField[ShipyardError, String](_.kind, _.toString)(
    "resourceNotFoundError" -> ResourceNotFoundError.schema,
    "lighthouseError" -> LighthouseError.schema,
  )

  def mapToShipyardError[Err <: ShipyardError](error: Err): ShipyardError =
    new ShipyardError {
      override def message: String = error.message

      override def kind: String = error.kind
    }
}

case class ResourceNotFoundError(resourceId: String, resourceKind: String) extends ShipyardError {
  override def message: String = s"Resource ${resourceKind} not found with UUID `${resourceId}`"
  override def kind: String = "resourceNotFoundError"
}

object ResourceNotFoundError {
  implicit val encoder: Encoder[ResourceNotFoundError] = deriveEncoder
  implicit val decoder: Decoder[ResourceNotFoundError] = deriveDecoder

  implicit val schema: Schema[ResourceNotFoundError] = Schema.derived[ResourceNotFoundError]
}

case class LighthouseError(message: String) extends ShipyardError {
  override def kind: String = "lighthouseError"
}
object LighthouseError {
  implicit val encoder: Encoder[LighthouseError] = deriveEncoder
  implicit val decoder: Decoder[LighthouseError] = deriveDecoder

  implicit val schema: Schema[LighthouseError] = Schema.derived[LighthouseError]
}

case class ShipmentError(shipmentId: String) extends ShipyardError {
  override def message: String = shipmentId

  override def kind: String = "shipmentError"
}

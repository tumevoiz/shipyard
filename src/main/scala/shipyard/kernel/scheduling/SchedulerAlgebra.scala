package shipyard.kernel.scheduling

import cats.Monad
import cats.data._
import cats.effect._
import cats.implicits._
import com.github.dockerjava.api.model.Container
import com.sun.management.OperatingSystemMXBean
import org.typelevel.log4cats.Logger
import shipyard.{ResourceNotFoundError, ShipyardError}
import shipyard.api.algebras.ShipmentAlgebra
import shipyard.api.endpoints.{ShipEndpoints, ShipmentEndpoints}
import shipyard.domain.{
  HardwareResources,
  IPAddress,
  Ship,
  ShipId,
  Shipment,
  ShipmentHealth,
  ShipmentId
}
import shipyard.kernel.{ApplicationRefState, PersistenceAlgebra}
import shipyard.runtime.ContainerRuntime
import sttp.client3.UriContext
import sttp.tapir.client.sttp.SttpClientInterpreter

import java.lang.management.ManagementFactory
import scala.concurrent.duration.DurationInt
import scala.util.control.Breaks.break

final case class SchedulerAlgebra[F[
    _
]: Async: Monad: ContainerRuntime: Logger: ShipmentAlgebra: PersistenceAlgebra](
    lhIP: IPAddress,
    shipName: String
) {
  private val prefix = "shipyard_scheduled_"

  private implicit val shipmentAlgebra = implicitly[ShipmentAlgebra[F]]
  def createShipment(shipment: Shipment): F[Unit] =
    for {
      _ <- Logger[F].info(s"Scheduling '${shipment.name}' shipment with image ${shipment.image}")
      _ <- ContainerRuntime[F].pullImage(
        splitImageFormat(shipment.image)._1,
        splitImageFormat(shipment.image)._2
      )
      containerId <- ContainerRuntime[F].createContainer(
        s"${prefix}${shipment.name}",
        shipment.image
      )
      _ <- Logger[F].info(s"Starting container with ID ${containerId}")
      _ <- ContainerRuntime[F].startContainer(containerId)
    } yield ()

  def deleteShipment(shipmentId: ShipmentId): F[Unit] =
    for {
      _ <- Logger[F].info(s"Requested deletion of shipment with ID ${shipmentId}")
      shipment <- shipmentAlgebra
        .findByUUID(shipmentId)
        .map(_.get)
        .orRaise(new ResourceNotFoundError(shipmentId.toString, "shipment"))
      containers <- ContainerRuntime[F].listContainer
      container <- Async[F].blocking(
        containers.find(_.getNames.contains(s"/${prefix}${shipment.name}"))
      )
      client = SttpClientInterpreter().toQuickClient(
        ShipmentEndpoints.delete,
        baseUri = uri"http://${lhIP}:8080".some
      )
      _ <- Monad[F].ifM(Monad[F].pure(container.isDefined))(
        ifTrue = Logger[F].info(
          s"Deleting the shipment ${shipment.name} with container ID ${container.get.getId}."
        ) *>
          ContainerRuntime[F]
            .kill(container.get.getId)
            .handleErrorWith(f => Logger[F].warn(s"Cannot kill container: ${f.getMessage}")) *>
          Async[F].blocking(client(shipmentId)) *>
          Logger[F].info("Removed shipment."),
        ifFalse = Async[F].pure(())
      )
    } yield ()

  private def splitImageFormat(image: String): (String, String) = {
    val splitted = image.split(':')
    (splitted(0), splitted(1))
  }

  // TODO will be moved?
  private def updateShipResources(refState: Ref[F, ApplicationRefState]): F[Unit] =
    (for {
      id <- OptionT(getShipIdByName(refState, shipName))
      osBeans: OperatingSystemMXBean = ManagementFactory
        .getOperatingSystemMXBean()
        .asInstanceOf[OperatingSystemMXBean]
      memory = osBeans.getFreePhysicalMemorySize / 1024 / 1024
      cpu = osBeans.getSystemCpuLoad
      client = SttpClientInterpreter().toQuickClient(
        ShipEndpoints.updateResources,
        baseUri = uri"http://${lhIP}:8080".some
      )
    } yield {
      client((id, HardwareResources(memory.toInt, cpu)))
    }).value.void *> Logger[F].info("Sent the metrics.")

  private def updateShipmentHealth(refState: Ref[F, ApplicationRefState]): F[Unit] =
    for {
      state <- refState.get
      containers <- ContainerRuntime[F].listContainer
      _ <- Logger[F].info("Sending health shipments info.")
      healthyShipments <- Async[F].blocking(
        state.shipments
          .filter { shipment =>
            containers.exists(_.getNames.contains(s"/${prefix}${shipment.name}"))
          }
          .filterNot(_.health == ShipmentHealth.Detached.some)
      )
      client = SttpClientInterpreter().toQuickClient(
        ShipmentEndpoints.setStatus,
        baseUri = uri"http://${lhIP}:8080".some
      )
      _ <- healthyShipments.traverse(s =>
        Async[F].blocking(client((s.id.get, ShipmentHealth.Running)))
      )
      detachedShipments <- Async[F].blocking(
        state.shipments.filter(_.health == ShipmentHealth.Detached.some)
      )
      _ <- detachedShipments.traverse_(s => deleteShipment(s.id.get))
      _ <- Logger[F].info("Removing detached resources.")
    } yield ()

  def runShipTasks(persistenceAlgebra: PersistenceAlgebra[F]): F[Unit] =
    Async[F].whileM_(Async[F].pure(true)) {
      (for {
        _ <- Async[F].sleep(10 seconds)
        _ <- Logger[F].info("Running ShipScheduler.")
        _ <- Logger[F].info("[ship] Dialing to Lighthouse to get fresh state.")
        refState <- persistenceAlgebra
          .loadFromLighthouse(lhIP, shipName)
          .value
          .flatMap(s => Ref[F].of(s.toOption.get))
        unscheduled <- pullUnscheduledShipments(refState)
        _ <- unscheduled
          .traverse_(s => createShipment(s))
          .handleErrorWith(e => Logger[F].error(e)("[ship] Cannot schedule shipment."))
        _ <- updateShipResources(refState)
        _ <- updateShipmentHealth(refState)
        _ <- Logger[F].info("[ship] Reconciled.")
      } yield ()).handleErrorWith(e => Logger[F].error(e)("[ship] An error occurred in scheduler!"))
    }

  def runLhTasks(refState: Ref[F, ApplicationRefState]): F[Unit] =
    Async[F].whileM_(Async[F].pure(true)) {
      (for {
        _ <- Async[F].sleep(10 seconds)
        _ <- Logger[F].info("Running LHScheduler.")
        _ <- attachShipmentsToShips(refState)
        state <- refState.get
        _ <- PersistenceAlgebra[F].saveToDisk(state) *> Logger[F].info("[lighthouse] Saved state!")
      } yield ()).handleErrorWith(e =>
        Logger[F].error(e)("[lighthouse] An error occurred in scheduler!")
      )
    }

  private def attachShipmentsToShips(refState: Ref[F, ApplicationRefState]): F[Unit] = {
    for {
      shipments <- refState.get.map(_.shipments)
      ships <- refState.get.map(_.ships)
      _ <- shipments.filter(_.shipId === None).traverse_ { shipment =>
        val tail = shipments.filterNot(_.id === shipment.id)
        val freeShip = ships
          .filterNot(_.lighthouse)
          .find { ship =>
            val shipPredictedMemory = shipments
              .filter(_.shipId === ship.id)
              .map(_.hardwareRequirements.getOrElse(HardwareResources.undefined).memory)
              .sum

            ship.currentHardwareResourcesUsage
              .getOrElse(HardwareResources.undefined)
              .memory - shipPredictedMemory >= shipment.hardwareRequirements
              .getOrElse(HardwareResources(0, 0))
              .memory
          }

        freeShip match {
          case Some(ship) => {
            refState.update { state =>
              state.copy(
                shipments = List(shipment.copy(shipId = ship.id)) ++ tail
              )
            } *> Logger[F].info(s"Scheduled '${shipment.name}' on '${ship.id}/${ship.name}'")
          }
          case None =>
            Logger[F].error("Cannot schedule shipment. Please adjust the hardware quota.")
        }
      }
    } yield ()
  }

  private def pullUnscheduledShipments(refState: Ref[F, ApplicationRefState]): F[List[Shipment]] =
    for {
      shipments <- refState.get.map(_.shipments.filterNot(_.health == ShipmentHealth.Detached.some))
      dockerContainers <- ContainerRuntime[F].listContainer
      shipId <- getShipIdByName(refState, shipName)
    } yield shipments
      .filterNot { shipment =>
        dockerContainers.exists(_.getNames.contains(s"/${prefix}${shipment.name}"))
      }
      .filter(_.shipId === shipId)

  private def getShipIdByName(
      refState: Ref[F, ApplicationRefState],
      shipName: String
  ): F[Option[ShipId]] =
    refState.get.map(_.ships.find(_.name === shipName).flatMap(_.id))
}

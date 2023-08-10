package shipyard_tests.it

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import shipyard.runtime.DockerRuntime

import scala.concurrent.duration.DurationInt

class ContainerRuntimeSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  "An container runtime" - {
    "create a new container and start it" in {
      val containerRuntime = DockerRuntime[IO]
      val testedContainerName = "nginx-test-cr-tests"
      for {
        _ <- containerRuntime.pullImage("nginx")
        _ <- IO.print("Waiting 10s for nginx image pull.")
        _ <- IO.sleep(10 seconds)
        containerId <- containerRuntime.createContainer(testedContainerName, "nginx:latest")
        _ <- containerRuntime.startContainer(containerId)
        containers <- containerRuntime.listContainer
        testedContainer = containers.filter(_.getId === containerId).head
        _ <- IO.print(
          s"Test container name: ${testedContainer.getNames.mkString("Array(", ", ", ")")}"
        )
        _ <- IO(testedContainer).asserting(_.getNames.isEmpty shouldBe false)
        _ <- IO(testedContainer).asserting(
          _.getNames.contains(s"/$testedContainerName") shouldBe true
        )
      } yield ()
    }
  }
}

package shipyard.runtime

import cats.implicits._
import cats.effect.{Async, Sync}
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.{
  Container,
  ExposedPort,
  PortBinding,
  PullResponseItem,
  Statistics
}
import com.github.dockerjava.core.{DefaultDockerClientConfig, DockerClientImpl}
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import org.typelevel.log4cats.Logger

import java.io.Closeable
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters._

case class DockerRuntime[F[_]: Async](client: DockerClient) extends ContainerRuntime[F] {
  override def createContainer(name: String, image: String): F[String] =
    Async[F].blocking(
      client
        .createContainerCmd(image)
        .withName(name)
        .withPortBindings(PortBinding.parse("8010:80"))
        .exec()
        .getId
    )

  override def startContainer(containerId: String): F[Unit] =
    Async[F].blocking(client.startContainerCmd(containerId).exec())

  override def listContainer: F[List[Container]] =
    Async[F].blocking(client.listContainersCmd().withShowAll(true).exec().asScala.toList)

  override def pullImage(image: String, tag: String = "latest"): F[Unit] =
    Async[F].blocking(client.pullImageCmd(image).withTag(tag).exec(new PullImageResultCallback()))

  override def kill(containerId: String): F[Unit] =
    Async[F].blocking(client.killContainerCmd(containerId).exec())
}

object DockerRuntime {
  implicit def alegbra[F[_]: Async]: DockerRuntime[F] = {
    val defaultConfig = DefaultDockerClientConfig
      .createDefaultConfigBuilder()
      .build()
    val httpClient =
      new ZerodepDockerHttpClient.Builder().dockerHost(defaultConfig.getDockerHost).build()
    DockerRuntime(DockerClientImpl.getInstance(defaultConfig, httpClient))
  }

  def apply[F[_]: Async]: DockerRuntime[F] = implicitly[DockerRuntime[F]]
}

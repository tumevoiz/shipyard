package shipyard.runtime

import com.github.dockerjava.api.model.Container

trait ContainerRuntime[F[_]] {
  def createContainer(name: String, image: String): F[String]
  def startContainer(containerId: String): F[Unit]
  def pullImage(image: String, tag: String = "latest"): F[Unit]
  def listContainer: F[List[Container]]
  def kill(containerId: String): F[Unit]
}

object ContainerRuntime {
  def apply[F[_]](implicit cr: ContainerRuntime[F]): ContainerRuntime[F] = implicitly[ContainerRuntime[F]]
}
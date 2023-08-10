ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

// scalafix
//ThisBuild / semanticdbEnabled := true
//ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
//ThisBuild / scalafixScalaBinaryVersion := "2.13"

val circeVersion = "0.14.1"
val dockerVersion = "3.3.0"
val enumeratumVersion = "1.7.2"
val fs2Version = "3.6.1"
val http4sVersion = "0.23.18"
val tapirVersion = "1.2.8"

lazy val root = (project in file("."))
  .settings(
    name := "shipyard",
    libraryDependencies ++= Seq(
      // Functional Programming
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.4.6",
      "com.beachape" %% "enumeratum" % enumeratumVersion,
      "com.beachape" %% "enumeratum-cats" % enumeratumVersion,
      "com.beachape" %% "enumeratum-circe" % enumeratumVersion,

      // fs2
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,

      // Logging
      "org.typelevel" %% "log4cats-slf4j" % "2.5.0",
      "ch.qos.logback" % "logback-classic" % "1.4.6",

      // JSON libraries
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-fs2" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.http4s" %% "http4s-client" % http4sVersion,
      "org.http4s" %% "http4s-server" % http4sVersion,

      // API documentation
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-client" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-enumeratum" % tapirVersion,

      // Docker runtime
      "com.github.docker-java" % "docker-java" % dockerVersion,
      "com.github.docker-java" % "docker-java-transport-zerodep" % dockerVersion,

      // Tests
      "org.scalactic" %% "scalactic" % "3.2.15",
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0" % Test,
      "org.scalatest" %% "scalatest" % "3.2.15" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "utf8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-unchecked",
      "-Ymacro-annotations",
      "-Ywarn-unused"
    )
  )

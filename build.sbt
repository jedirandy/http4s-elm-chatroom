import Dependencies._

val http4sVersion = "0.17.5"
val circeVersion = "0.8.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "neko",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "0.9.7",
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-literal" % circeVersion % Test,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      scalaTest % Test
    )
  )

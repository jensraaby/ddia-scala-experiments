import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.jensraaby",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "DDIA Experiments",
    libraryDependencies += scalaTest % Test
  )

lazy val benchmarks = (project in file ("benchmarks")).dependsOn(root).enablePlugins(JmhPlugin)

lazy val graphql = (project in file ("graphql"))
  .settings(
    libraryDependencies ++= circe :+ sangria :+ sangriaCirce
  )

lazy val akkaStreaming = (project in file ("akka-streaming"))

lazy val rest = (project in file ("rest"))

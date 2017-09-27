import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"

  lazy val sangria = "org.sangria-graphql" %% "sangria" % "1.3.0"
  lazy val sangriaCirce = "org.sangria-graphql" %% "sangria-circe" % "1.1.0"

  lazy val circeVersion = "0.8.0"

  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)
}

scalaVersion in ThisBuild := "2.11.11"
name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.11.11"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "2.4.4",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.4"
)

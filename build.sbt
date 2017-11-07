name := "a2017"

version := "1.0"

scalaVersion := "2.11.11"

libraryDependencies ++= {
  val akka = "com.typesafe.akka"
  val akkaV = "2.4.20"
  val akkaHttpV = "10.0.6"
  val scalaTestV = "3.0.0"
  Seq(
    akka                        %% "akka-actor"                           % akkaV,
    akka                        %% "akka-http-core"                       % akkaHttpV,
    akka                        %% "akka-http-spray-json"                 % akkaHttpV,
  )
}

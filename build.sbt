name := "a2017"

version := "1.0"

scalaVersion := "2.11.11"

libraryDependencies ++= {
  val akkaV = "2.4.20"
  val akkaHttpV = "10.0.6"
  val scalaTestV = "3.0.0"
  Seq(
    "org.apache.spark" %% "spark-core" % "2.2.0",
    "org.apache.spark" %% "spark-sql" % "2.2.0",
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV
//    akka                        %% "akka-actor"                           % akkaV,
//    akka                        %% "akka-http-spray-json"                 % akkaHttpV
  )

}

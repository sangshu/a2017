name := "a2017"

version := "1.0"

scalaVersion := "2.11.11"

libraryDependencies ++= {
  val akkaV = "2.5.4"
  val akkaHttpV = "10.0.10"
  val spark_version = "2.2.0"
  Seq(
    "org.apache.spark" %% "spark-core" % spark_version,
    "org.apache.spark" %% "spark-sql" % spark_version,
    "org.apache.spark" %% "spark-streaming" % spark_version,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "org.apache.bahir" %% "spark-streaming-akka" % spark_version

    //"com.typesafe.akka" %% "akka-stream-experimental" % "2.0.5",
    //"org.twitter4j" % "twitter4j-stream" % "4.0.3"
//    akka                        %% "akka-actor"                           % akkaV,
//    akka                        %% "akka-http-spray-json"                 % akkaHttpV
  )

}

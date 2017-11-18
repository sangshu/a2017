import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.akka.AkkaUtils
import org.apache.spark.streaming.{Milliseconds, StreamingContext}

import scala.concurrent.duration._

object SparkStream {
  val akkaConf = ConfigFactory.parseString(
    s"""akka.actor.provider = "akka.remote.RemoteActorRefProvider"
       |akka.remote.enabled-transports = ["akka.remote.netty.tcp"]
       |akka.remote.netty.tcp.hostname = "$AkkaStream.host"
       |akka.remote.netty.tcp.port = $AkkaStream.port
       |""".stripMargin)



  implicit val system = ActorSystem("pinttest", akkaConf)

  import system.dispatcher

  def textStream(text: String): Unit = {
    tester ! text
  }
}

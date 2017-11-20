
import akka.Done
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.types.StructType

import scala.collection.mutable
import scala.concurrent.{Future, Promise}

object TickFeederActor {
  case class SubscribeReceiver(receiverActor: ActorRef)
  case class UnsubscribeReceiver(receiverActor: ActorRef)
  case object StopFeeder

  private val GDAX_WS = WebSocketRequest("wss://ws-feed.gdax.com")
//  val host = "127.0.0.1"
//  val port = "9992"
  val name = "gdaxclient"

//  val akkaConf = ConfigFactory.parseString(
//    s"""akka.actor.provider = "akka.remote.RemoteActorRefProvider"
//       |akka.remote.enabled-transports = ["akka.remote.netty.tcp"]
//       |akka.remote.netty.tcp.hostname = "$host"
//       |akka.remote.netty.tcp.port = $port
//       |""".stripMargin)
  val message2: TextMessage = TextMessage("{\"type\":\"subscribe\"," +
                                       "\"channels\":[{\"name\":\"ticker\"," +
                                       "\"product_ids\":[\"BTC-USD\"]}]}")

  val message: TextMessage = TextMessage("{\"type\":\"subscribe\",\"product_ids\":[\"BTC-USD\"]," +
                                         "\"channels\":[\"full\"]}")
    //"""{"type":"subscribe","product_ids":["ETH-USD","BTC-USD"],"channels":["full"]}""")

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def props:Props = Props(new TickFeederActor())
  //val feeder = system.actorOf(Props[TickFeederActor],"BTCUSD")

  private val sample1 = "{\"type\":\"ticker\",\"sequence\":4392902023,\"product_id\":\"BTC-USD\"," +
                  "\"price\":\"7812.00000000\",\"open_24h\":\"7643.00000000\",\"volume_24h\":\"10102.71105424\",\"low_24h\":\"7812.00000000\",\"high_24h\":\"7847.98000000\",\"volume_30d\":\"625869.28881853\",\"best_bid\":\"7811.99\",\"best_ask\":\"7812\",\"side\":\"buy\",\"time\":\"2017-11-19T04:03:41.992000Z\",\"trade_id\":24525825,\"last_size\":\"0.00000127\"}"
  private val sample2 = """{"type":"open","side":"buy","price":"368.03000000","order_id":"22153e4e-50b7-4320-8481-28477fcf4bfb","remaining_size":"3.00000000","product_id":"ETH-USD","sequence":1513318609,"time":"2017-11-20T20:40:55.321000Z"}"""
  val keys = sample2.replace("{","").replace("}","").split(",\"").map(_.split("\":")(0).replace("\"",""))
}

class TickFeederActor extends Actor {
  import TickFeederActor._

  private val log = Logging(context.system, this)
  private val receivers = new mutable.LinkedHashSet[ActorRef]()

  def receive: Receive = {
    case SubscribeReceiver(receiverActor: ActorRef) =>
      println(s"received subscribe from ${receiverActor.toString}")
      receivers += receiverActor

    case UnsubscribeReceiver(receiverActor: ActorRef) =>
      println(s"received unsubscribe from ${receiverActor.toString}")
      receivers -= receiverActor

    case StopFeeder => promise match {
      case x: Promise[Option[Message]] => x.success(None)
      case _ => log.info(s"done> $message")
    }
  }

  def feed(ticker: Array[String]): Unit = {
    receivers.foreach(_ ! ticker)
  }

  private val outgoing = Source.single[Message](message)
    .concatMat(Source.maybe[Message])(Keep.right)

  private val incoming: Sink[Message, Future[Done]] =
    Sink.foreach {
      case message: TextMessage.Strict =>
        //println(s">>out: $message")
        val values = message.text.split(",\"").map(_.split("\":")(1))
        feed(values) //Array[String]
      case _ => log.error(s"received unknown message format. $GDAX_WS; $message")
    }

  val flow: Flow[Message, Message, Promise[Option[Message]]] =
    Flow.fromSinkAndSourceMat(incoming, outgoing)(Keep.right)

  val (upgradeResponse, promise) = Http().singleWebSocketRequest(GDAX_WS, flow)
}

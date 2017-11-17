import java.util.concurrent.TimeUnit

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMessage.HttpMessageScalaDSLSugar
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.apache.ivy.ant.FixDepsTask
import org.apache.spark.metrics
import org.apache.spark.metrics.source

import scala.concurrent.{Future, Promise}


object AkkaStream extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  import system.dispatcher


  def message = TextMessage.Strict("{\"type\":\"subscribe\",\"channels\":[{\"name\":\"ticker\"," +
    "\"product_ids\":[\"BTC-USD\"]}]}")

  val outgoing = Source.single[Message](message).concatMat(Source.maybe[Message])(Keep.right)


  val incoming: Sink[Message, Future[Done]] =
    Sink.foreach {
      case message: TextMessage.Strict =>
        //println(s"out: $message")
        SparkStream.textStream(message.text)
      case _ => println(s"received unknown message format")
    }

  val flow: Flow[Message, Message, Promise[Option[Message]]] =
    Flow.fromSinkAndSourceMat(incoming, outgoing)(Keep.right)

  val (upgradeResponse, promise) =
    Http().singleWebSocketRequest(WebSocketRequest("wss://ws-feed.gdax.com"), flow)

  //promise.success(None)
}
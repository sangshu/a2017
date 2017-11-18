import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, SourceQueue}
import akka.stream.{ActorMaterializer, OverflowStrategy}

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}

object GdaxWebSockets extends App {
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()
  import actorSystem.dispatcher


  // print each incoming strict text message
  val printSink: Sink[Message, Future[Done]] = Sink.foreach {
    case message: TextMessage.Strict =>
      //println(s"out: $message")
      SparkStream.textStream(message.text)
    case _ => println(s"received unknown message format")
  }

  val (source, sourceQueue) = {
    val p = Promise[SourceQueue[Message]]
    val s = Source.queue[Message](Int.MaxValue, OverflowStrategy.backpressure).mapMaterializedValue(m => {
      p.trySuccess(m)
      m
    })
      .keepAlive(FiniteDuration(10, TimeUnit.SECONDS), () =>
        TextMessage.Strict("{\"type\":\"subscribe\",\"channels\":[{\"name\":\"ticker\"," +
          "\"product_ids\":[\"BTC-USD\"]}]}"))
    (s, p.future)
  }


  val flow =
    Flow.fromSinkAndSourceMat(printSink, source)(Keep.right)


  val (upgradeResponse, sourceClosed) =
    Http().singleWebSocketRequest(WebSocketRequest("wss://ws-feed.gdax.com"), flow)

  val connected = upgradeResponse.map { upgrade =>
    if (upgrade.response.status == StatusCodes.SwitchingProtocols || upgrade.response.status == StatusCodes.OK) {
      Done
    } else {
      throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
    }
  }

  println("Hi Mel")
  connected.onComplete(println)

}

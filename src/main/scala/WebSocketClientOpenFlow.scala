import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.Promise

object WebSocketClientOpenFlow {
  def main(args: Array[String]):Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    // using emit "one" and "two" and then keep the connection open
    val flow: Flow[Message, Message, Promise[Option[Message]]] =
      Flow.fromSinkAndSourceMat(
        Sink.foreach[Message](println),
        Source(List(TextMessage("{\"type\":\"subscribe\",\"channels\":[{\"name\":\"heartbeat\"," +
          "\"product_ids\":[\"ETH-EUR\"]}]}")))
          .concatMat(Source.maybe[Message])(Keep.right))(Keep.right)

    val (upgradeResponse, promise) =
      Http().singleWebSocketRequest(
        WebSocketRequest("wss://ws-feed.gdax.com"),
        flow)

    // at some later time we want to disconnect
    promise.success(None)
  }
}
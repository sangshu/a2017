import java.util.concurrent.TimeUnit

import akka.{Done, NotUsed}
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMessage.HttpMessageScalaDSLSugar
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.apache.ivy.ant.FixDepsTask
import org.apache.spark.{SparkConf, metrics}
import org.apache.spark.metrics.source
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.akka.AkkaUtils
import org.apache.spark.streaming.{Milliseconds, StreamingContext}

import scala.concurrent.{Future, Promise}


object AkkaStream extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  import system.dispatcher


  val spark: SparkSession = args.length match {
    case 1 => println("local <===");
      SparkSession
        .builder
        .master("local")
        .appName("StructuredNetworkWordCount")
        .getOrCreate()
    case 0 => println("remote <===");
      SparkSession
        .builder
        .appName("StructuredNetworkWordCount")
        .getOrCreate()
  }


  import spark.implicits._

  val ssc: StreamingContext = new StreamingContext(spark.sparkContext, Milliseconds(500))
  val host = "127.0.0.1"
  val port = "9999"
  val lines = AkkaUtils.createStream[String](
    ssc,
    Props(classOf[BridgeActor[String]], s"akka.tcp://test@$host:${port.toInt}/user/BridgeActor"),
    "BridgeActor")

  ssc.start()
  ssc.awaitTermination()


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

  val checker = new Thread(new Runnable {
    override def run(): Unit = {
      while (true) {
        print(".")
        lines.map(println)
        Thread.sleep(300)
      }


    }
  }).start()
  //promise.success(None)
}
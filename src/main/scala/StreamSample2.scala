import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes.`text/plain(UTF-8)`
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.util.{Failure, Success}

object StreamSample2 extends App{
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  import system.dispatcher


  val numbers =
    Source.unfold(0L) { (n) =>
      val next = n + 1
      Some((next, next))
    }.map(n => ByteString(n + "\n"))

  val route =
    path("numbers") {
      get {
        complete(
          HttpResponse(entity = HttpEntity(`text/plain(UTF-8)`, numbers))
        )
      }
    }

  val futureBinding = Http().bindAndHandle(route, "127.0.0.1", 8080)

  futureBinding.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      println(s"Akka HTTP server running at ${address.getHostString}:${address.getPort}")

    case Failure(ex) =>
      println(s"Failed to bind HTTP server: ${ex.getMessage}")
      ex.fillInStackTrace()

  }
}
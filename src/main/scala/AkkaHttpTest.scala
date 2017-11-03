import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

object HelloWorld{
  def main(args:Array[String]):Unit = {
    println("HelloWorld")

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val http = Http()
    val response: Future[HttpResponse] =
      http.singleRequest(HttpRequest(uri = "http://akka.io"))

    val result = response.map {
      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
        Unmarshal(entity).to[String]
        http.shutdownAllConnectionPools()
      case x => s"Unexpected status code ${x.status}"
    }
    println(Await.result(result, 10.seconds))

     http.shutdownAllConnectionPools()
    system.terminate()
  }
}

import scala.concurrent.{ Await, Future }
import akka.http.scaladsl.unmarshalling.Unmarshal
import javax.xml.bind.Unmarshaller

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import scala.concurrent.duration._

object HelloWorld{
  def main(args:Array[String]):Unit = {
    println("HelloWorld")

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val http = Http()
    val response: Future[HttpResponse] =
      http.singleRequest(HttpRequest(uri = "http://www.webscantest.com"))

    val result = response.map {
      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
        println(entity)
        Unmarshal(entity).to[String]
        http.shutdownAllConnectionPools()
      case x => s"Unexpected status code ${x.status}"
    }
    println(Await.result(result, 10.seconds))

     http.shutdownAllConnectionPools()
    system.terminate()
  }
}

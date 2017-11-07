import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}


object StreamSample1 {
  def main(args:Array[String]):Unit ={
    implicit  val system = ActorSystem()
    implicit  val mat = ActorMaterializer()

    val source = Source(0 to 20000000)
    val flow = Flow[Int].map(_.toString());
    val sink = Sink.foreach[String](println(_))
    val runnable = source.via(flow).to(sink)
    runnable.run()
  }

}

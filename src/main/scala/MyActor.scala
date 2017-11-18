import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging

class MyActor extends Actor{
  val log = Logging(context.system, this)

  def receive = {
    case "test" => log.info("myactor received a test")
    case _  => log.info("nonono")
  }

}

object Test extends App{
  implicit val system = ActorSystem("pinttest")
  val tester = system.actorOf(Props[MyActor], "tester")

  import system.dispatcher

  tester ! "hi"
  tester ! "test"



}

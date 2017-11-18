import akka.actor.ActorRef
import akka.event.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.akka.ActorReceiver

case class SubscribeReceiver(receiverActor: ActorRef)
case class UnsubscribeReceiver(receiverActor: ActorRef)

class BridgeActor[T](urlOfPublisher: String) extends ActorReceiver {
  val log = Logging(context.system, this)
  lazy private val remotePublisher = context.actorSelection(urlOfPublisher)

  override def preStart(): Unit = remotePublisher ! SubscribeReceiver(context.self)

  def receive: PartialFunction[Any, Unit] = {
    case msg => store(msg.asInstanceOf[T])
  }

  override def postStop(): Unit = remotePublisher ! UnsubscribeReceiver(context.self)

}


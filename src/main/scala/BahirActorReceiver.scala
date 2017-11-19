import org.apache.spark.streaming.akka.ActorReceiver

/**
  * A sample actor as receiver, is also simplest. This receiver actor
  * goes and subscribe to a typical publisher/feeder actor and receives
  * data.
  *
  */
class BahirActorReceiver[T] extends ActorReceiver {
  lazy private val remotePublisher = context.actorOf(TickFeederActor.props, "BTCUSD")

  import TickFeederActor._

  override def preStart(): Unit = println("SHU-start"); remotePublisher ! SubscribeReceiver(context.self)

  def receive: PartialFunction[Any, Unit] = {
    case msg => store(msg.asInstanceOf[T])
  }

  override def postStop(): Unit = remotePublisher ! UnsubscribeReceiver(context.self)

}

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import com.typesafe.config.ConfigFactory
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.streaming.akka.AkkaUtils

object GdaxClient extends App {
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  val sparkConf = new SparkConf().setAppName(GdaxClient.getClass.getName)

  // check Spark configuration for master URL, set it to local if not configured
  if (!sparkConf.contains("spark.master")) {
    sparkConf.setMaster("local[2]")
  }

  val spark: SparkSession = if (sparkConf.contains("spark.master"))
    SparkSession
      .builder
      .master("local[2]")
      .appName("StructuredNetworkWordCount")
      .getOrCreate()
  else
    SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .getOrCreate()

  import spark.implicits._

  val ssc: StreamingContext = new StreamingContext(spark.sparkContext, Milliseconds(100))
  //  val host = "127.0.0.1"
  //  val port = "9992"
  val name = "gdaxclient"
  val lines = AkkaUtils.createStream[Array[String]](ssc,
    Props(classOf[BahirActorReceiver[Array[String]]]),
    "BTCUSD")

  lines.foreachRDD {
    rdd => rdd.foreach(dd ⇒ {dd.foreach(a ⇒ print(s"$a,"));println})
  }


  ssc.start()
  ssc.awaitTermination()
}

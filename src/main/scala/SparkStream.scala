import org.apache.spark.sql.SparkSession

object SparkStream {
  var spark:SparkSession = spark
  if (GdaxWebSockets.local) {
    spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .getOrCreate()
  }

  val lines = spark.readStream.format("z")

  def textStream(text: String): Unit = {
    println(text)
  }
}

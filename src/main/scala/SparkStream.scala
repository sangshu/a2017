import org.apache.spark.sql.SparkSession

object SparkStream {
  val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .getOrCreate()

  import spark.implicits._

  val lines = spark.readStream.format("z")

  def textStream(text: String): Unit = {
    println(text)
  }
}

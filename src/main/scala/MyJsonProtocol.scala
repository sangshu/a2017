import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.json._
import DefaultJsonProtocol._

final case class GdaxFullResponse(_type: String, side: String, order_id: String, reason: String, product_id: String,
                                  price: Double,
                                  remaining_size: Double, sequence: Long, time: String)


object MyJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object GdaxFullResponseFormat extends RootJsonFormat[GdaxFullResponse] {
    def write(info: GdaxFullResponse) = JsObject(
      "type" -> JsString(info._type),
      "side" -> JsString(info.side),
      "order_id" -> JsString(info.order_id),
      "reason" -> JsString(info.reason),
      "product_id" -> JsString(info.product_id),
      "price" -> JsString(info.price.toString),
      "remaining_size" -> JsString(info.remaining_size.toString),
      "sequence" -> JsNumber(info.sequence),
      "time" -> JsString(info.time)
    )

    def read(value: JsValue) = {
      value.asJsObject.getFields("type", "side", "order_id", "reason", "product_id", "price", "remaining_size", "sequence",
        "time") match {
        case Seq(JsString(_type), JsString(side), JsString(order_id), JsString(reason), JsString(product_id), JsString
          (price), JsString(remaining_size), JsNumber(sequence), JsString(time)) ⇒
          new GdaxFullResponse(_type, side, order_id, reason, product_id, price.toDouble, remaining_size.toDouble, sequence
            .toLong, time)
        case _ ⇒ throw new DeserializationException("FullInfo expected")
      }
    }

  }

  def main(args:Array[String]) {
    val full =
      """{"type":"done","side":"sell","order_id":"77488cbc-7a5f-498b-89c9-94ffd108463b","reason":"canceled","product_id":"ETH-USD","price":"783.70000000",
        |"remaining_size":"0.04660472","sequence":1732408534,"time":"2017-12-19T00:48:30.195000Z"}"""

    val json = GdaxFullResponse("done", "sell", "77488dbc-7a5f-498b-89c9-94ffd1108463b", "canceled", "eth-usd", 783.70000,
      0.0466, 1732, "2017").toJson

    println(json)
    val jj = json.convertTo[GdaxFullResponse]
    println(jj)
  }
}
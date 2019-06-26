package utils

//#json-support
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import api.Services.{ActionPerformed, FetchData}
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val fetchDataJsonFormat = jsonFormat2(FetchData)

  implicit val actionPerformedJsonFormat = jsonFormat2(ActionPerformed)
}
//#json-support
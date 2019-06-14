package sources

import java.nio.charset.StandardCharsets

import akka.stream.{ActorMaterializer, Materializer}
import mongo.MongoDAO
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.libs.ws.ahc.{AhcCurlRequestLogger, StandaloneAhcWSClient}
import play.api.libs.ws.DefaultBodyReadables._
import play.api.libs.ws.DefaultBodyWritables._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._
import com.typesafe.config.{Config, ConfigFactory}
import org.mongodb.scala.bson.BsonString

import scala.concurrent.Await

object ErrorHandlers {

  val conf: Config = ConfigFactory.load()

  def copernicusODataErrorHandler(status: Int, body: Array[Byte], statusText: String, materializer: ActorMaterializer): Unit = {
    if (status == 500) {
      val responseStr = new String(body, StandardCharsets.UTF_8)
      if (responseStr.contains("Unexpected nav segment Navigation Property")) // resource does not exist, dont try again
        println("Resource doesnt exist, dont retry") // TODO log error
    }
    else if (status == 403 /*rate limit*/ || status == 500)
      throw new Exception(statusText + "->\n" + new String(body))
  }

  def earthExplorerErrorHandler(status: Int, body: Array[Byte], statusText: String, materializer: ActorMaterializer): Unit = {
    if (status == 200) {
      val responseStrTry = Try(new String(body, StandardCharsets.UTF_8))
      responseStrTry match {
        case Failure(_) =>
        case Success(responseStr) =>
          if (responseStr.contains("RATE_LIMIT"))
            throw new Exception(new String(body))
          else if (responseStr.contains("AUTH_UNAUTHORIZED")) {

            implicit val mat: ActorMaterializer = materializer

            val username = conf.getString("sources.earth-explorer.credentials.username")
            val password = conf.getString("sources.earth-explorer.credentials.pwd")

            val newTokenUrl = "https://earthexplorer.usgs.gov/inventory/json/v/1.4.0/login?jsonRequest=" +
              s"""{"username":"$username","password":"$password","catalogId":"EE"}"""

            val wsClient = StandaloneAhcWSClient()
            val wsClientUrl = wsClient.url(newTokenUrl)


            val req = wsClientUrl
              .get
              .map { response =>
                val body = response.body[String]
                val token = (Json.parse(body) \ "data").as[String]

                MongoDAO.updateToken("token", BsonString(token), MongoDAO.EARTH_EXPLORER_TOKENS)
              }
              .andThen { case _ => wsClient.close() }

            Await.result(req, 10000 millis)
            throw new Exception("AUTH_UNAUTHORIZED")
          }
      }

    }
    else if (status == 500)
      throw new Exception(statusText + "->\n" + new String(body))
  }


  def defaultErrorHandler(status: Int, body: Array[Byte], statusText: String, materializer: ActorMaterializer): Unit = {
    if (status == 500)
      throw new Exception(statusText + "->\n" + new String(body))
  }

  def creodiasErrorHandler(status: Int, body: Array[Byte], statusText: String, materializer: ActorMaterializer): Unit = {

    if (status == 200) {
      val responseStrTry = Try(new String(body, StandardCharsets.UTF_8))
      responseStrTry match {
        case Failure(_) =>
        case Success(responseStr) =>
          val json = Json.parse(responseStr)
          if ((json \ "features").as[List[JsValue]].isEmpty)
            throw new Exception("Product does not exist in CREODIAS")

      }
    }

    if (status == 500)
      throw new Exception(statusText + "->\n" + new String(body))
  }

}



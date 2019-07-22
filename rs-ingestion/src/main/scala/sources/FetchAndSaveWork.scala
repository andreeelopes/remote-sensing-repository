package sources

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import mongo.MongoDAO
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}
import play.api.libs.json._
import play.api.libs.ws.{DefaultBodyReadables, DefaultWSCookie, WSAuthScheme}
import play.api.libs.ws.ahc.{AhcCurlRequestLogger, StandaloneAhcWSClient}
import play.libs.ws.WSCookie
import sources.handlers.{AuthConfig, ErrorHandlers}
import utils.AuthException
import utils.HTTPClient.singleRequest
import utils.Utils.{ProductConf, writeFile}
import DefaultBodyReadables._

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class FetchAndSaveSource(configName: String,
                         val isEarthExplorer: Boolean = false,
                         val productType: String,
                         val fetchingProvider: String,
                         override val authConfigOpt: Option[AuthConfig] = None,
                         val errorHandler: (Int, Array[Byte], String, ActorMaterializer) => Unit = ErrorHandlers.defaultErrorHandler)
  extends Source(configName)

class FetchAndSaveWork(override val source: FetchAndSaveSource,
                       productId: String,
                       dataObjectId: String,
                       url: String,
                       timeout: FiniteDuration,
                       fileName: String) extends Work(source) {

  workTimeout = timeout

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    if (!source.isEarthExplorer)
      singleRequest(url, workTimeout, process, source.errorHandler, source.authConfigOpt)
    else {
      val wsClient = StandaloneAhcWSClient()

      var wsClientUrl =
        wsClient
          .url(url)
          .withFollowRedirects(true)
          .withRequestFilter(AhcCurlRequestLogger())

      val doc = MongoDAO.getDoc("cookies", MongoDAO.EARTH_EXPLORER_AUTH_COL).get
      val cookies = doc.getArray("cookies").getValues.asScala.map { c =>
        (c.asDocument().get("name").asString().getValue, c.asDocument().get("value").asString().getValue)
      }.toList

      cookies.foreach { c => wsClientUrl = wsClientUrl.addCookies(DefaultWSCookie(c._1, c._2)) }

      val wsClientAuth =
        if (source.isEarthExplorer && source.fetchingProvider != "search-data-nasa")
          wsClientUrl.withAuth(source.authConfigOpt.get.username, source.authConfigOpt.get.password, WSAuthScheme.BASIC)
        else if (source.fetchingProvider != "search-data-nasa") {
          val authConfig = Some(AuthConfig("search-data-nasa", source.config))
          wsClientUrl.withAuth(authConfig.get.username, authConfig.get.password, WSAuthScheme.BASIC)
        }
        else wsClientUrl

      wsClientAuth
        .get
        .map { response =>
          println(response.status)
          println(response.headers)
          process(response.body[Array[Byte]])
        }.andThen { case _ => wsClient.close() }
    }
  }

  override def process(responseBytes: Array[Byte]): List[Work] = {

    val dest = s"${source.baseDir}/$productId/$fileName"

    //    if (source.isEarthExplorer && source.productType != "MODIS_MYD13Q1_V6") {
    //      writeFile(dest, responseBytes)
    //
    //
    //    } else
    writeFile(dest, responseBytes)


    MongoDAO.updateProductData(productId, dataObjectId, Json.obj("status" -> "local", "url" -> dest), source.isEarthExplorer)

    List()
  }

}

package sources

import java.io.File
import java.nio.charset.StandardCharsets

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import org.joda.time.DateTime
import org.mongodb.scala.bson.{BsonDocument, BsonString}
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}
import utils.HTTPClient.singleRequest
import utils.Utils.dateFormat
import sources.handlers.ErrorHandlers._
import com.fasterxml.jackson.databind.node.ArrayNode
import com.jayway.jsonpath.JsonPath
import mongo.MongoDAO
import org.mongodb.scala.Document
import sources.handlers.ErrorHandlers
import sources.handlers.Parsing.{jsonConf, processExtractions}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class EarthExplorerSource(config: Config,
                          override val program: String,
                          override val platform: String,
                          override val productType: String)
  extends ProviderPeriodicRESTSource("earth-explorer", config, program, platform, productType) {
  val authConfigOpt: None.type = None

  val epochInitialWork = new EarthExplorerWork(this, ingestionHistoryDates, isEpoch = true)
  val periodicInitialWork = new EarthExplorerWork(this, initialIngestion)

  override final val PROVIDER: String = "earth-explorer"
  override val configName = "earth-explorer"
  final val metadataExt = "metadata"

}

class EarthExplorerWork(override val source: EarthExplorerSource,
                        override val ingestionDates: (DateTime, DateTime),
                        override val isEpoch: Boolean = false,
                        override val pageStart: Int = 0)
  extends ProviderPeriodicRESTWork(source, ingestionDates, isEpoch, pageStart) {

  val url = s"""${source.baseUrl}search?jsonRequest={"apiKey":"<token>","datasetName":"${source.productType}","temporalFilter":{"startDate":"${ingestionDates._1.toString(dateFormat)}","endDate":"${ingestionDates._2.toString(dateFormat)}"},"includeUnknownCloudCover":true,"maxResults":"${source.pageSize}","startingNumber":"$pageStart","sortOrder":"DESC"}"""

  private def getToken: String = {
    val tokenFuture: Future[Document] = MongoDAO.getDocField("token", "token", MongoDAO.EARTH_EXPLORER_TOKENS)
    val tokenEntry = Await.result(tokenFuture, 5000 millis)
    tokenEntry.get("token").get.asString().getValue
  }

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    val token = getToken

    val tokenUrl = url.replace("<token>", token)
    singleRequest(tokenUrl, workTimeout, process, ErrorHandlers.earthExplorerErrorHandler, source.authConfigOpt)
  }

  override def process(responseBytes: Array[Byte]): List[Work] = {
    val docJson = new String(responseBytes, StandardCharsets.UTF_8)
    val doc = Json.parse(docJson)
    var workToBeDone = List[Work]()

    getNextPagesWork(doc).foreach(w => workToBeDone ::= w)

    (doc \ "data" \ "results").as[List[JsObject]].foreach(entry => workToBeDone :::= processEntry(entry))

    saveFetchingLog(BsonDocument(docJson))

    workToBeDone
  }

  private def processEntry(node: JsValue) = {

    val entityId = (node \ "entityId").as[String]
    val title = (node \ "displayId").as[String]
    new File(s"${source.baseDir}/$entityId").mkdirs()

    setupEntryMongo(entityId)

    generateEEMetadataWork(entityId, title)
  }

  private def generateEEMetadataWork(entityId: String, title: String) = {
    val token = getToken

    val mdUrl = s"""${source.baseUrl}metadata?jsonRequest={"apiKey":"$token","datasetName":"${source.productType}","entityIds":"$entityId"}"""

    val mdExt = source.extractions.filter(e => e.name == source.metadataExt || e.context == source.metadataExt)

    val processEEMetadata = (responseBytes: Array[Byte]) => {
      val docStr = processExtractions(responseBytes, mdExt, entityId, mdUrl).right.get

      if (source.platform == "landsat8") {
        val doc = JsonPath.using(jsonConf).parse(docStr)

        val tier = doc.read[ArrayNode]("$.data..metadataFields[?(@.fieldName=='Collection Category')].value").get(0).asText
        if (tier == "T1" || tier == "RT") {

          val wrsPath = doc.read[ArrayNode]("$.data..metadataFields[?(@.fieldName=='WRS Path')].value").get(0).asText.trim
          val wrsRow = doc.read[ArrayNode]("$.data..metadataFields[?(@.fieldName=='WRS Row')].value").get(0).asText.trim

          val amazonProductUrl = s"http://landsat-pds.s3.amazonaws.com/c1/L8/$wrsPath/$wrsRow/$title"

          val mongoDoc = BsonDocument(
            "band1" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_B1.TIF")),
            "band2" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_B2.TIF")),
            "band3" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_B3.TIF")),
            "band4" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_B4.TIF")),
            "band5" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_B5.TIF")),
            "band6" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_B6.TIF")),
            "band7" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_B7.TIF")),
            "band8" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_B8.TIF")),
            "band9" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_B9.TIF")),
            "band10" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_B10.TIF")),
            "band11" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_B11.TIF")),
            "bandQA" -> BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(s"$amazonProductUrl/${title}_BQA.TIF")),
          )

          MongoDAO.addFieldToDoc(entityId, "data", mongoDoc, MongoDAO.PRODUCTS_COL)
        }
      }


      List()
    }


    if (mdExt.nonEmpty)
      List(
        new ExtractionWork(new ExtractionSource(source.config, source.configName, mdExt, earthExplorerErrorHandler, Some(processEEMetadata)),
          mdUrl, entityId)
      )
    else
      List()
  }


  def generatePeriodicWork(): EarthExplorerWork = {
    val updatedIngestionWindow = source.adjustIngestionWindow(ingestionDates)
    new EarthExplorerWork(source, updatedIngestionWindow)
  }

  override def getNextPagesWork(doc: JsValue): Option[Work] = {
    val nextRecord = (doc \ "data" \ "nextRecord").as[JsValue]

    if (nextRecord != JsNull)
      Some(new EarthExplorerWork(source, ingestionDates, isEpoch, pageStart + source.pageSize))
    else
      None
  }

}
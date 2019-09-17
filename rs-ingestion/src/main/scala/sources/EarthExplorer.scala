package sources

import java.io.File
import java.nio.charset.StandardCharsets

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import org.joda.time.DateTime
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonInt64, BsonString}
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}
import utils.HTTPClient.singleRequest
import utils.Utils.{dateFormat, generateUUID}
import sources.handlers.ErrorHandlers._
import mongo.MongoDAO
import sources.handlers.{AuthConfig, ErrorHandlers}
import sources.handlers.Parsing.{jsonConf, processExtractions}

class EarthExplorerSource(config: Config,
                          override val program: String,
                          override val platform: String,
                          override val productType: String)
  extends ProviderPeriodicSource("earth-explorer", config, program, platform, productType) {
  val authConfigOpt = Some(AuthConfig("earth-explorer", config))

  val epochWork: EarthExplorerWork = generateWork(historyDates, isEpoch = true)
  val periodicInitialWork: EarthExplorerWork = generateWork(initial)

  override final val PROVIDER: String = "earth-explorer"
  override val configName = "earth-explorer"
  final val metadataExt = "metadata"

  override def generateWork(intervalDates: (DateTime, DateTime), isEpoch: Boolean): EarthExplorerWork = {
    new EarthExplorerWork(this, intervalDates, isEpoch)
  }

}

class EarthExplorerWork(override val source: EarthExplorerSource,
                        override val intervalDates: (DateTime, DateTime),
                        override val isEpoch: Boolean = false,
                        override val pageStart: Int = 0)
  extends ProviderPeriodicWork(source, intervalDates, isEpoch, pageStart) {

  import sources.handlers.EarthExplorerUtils._

  val url = s"""${source.baseUrl}search?jsonRequest={"apiKey":"<token>","datasetName":"${source.productType}","temporalFilter":{"startDate":"${intervalDates._1.toString(dateFormat)}","endDate":"${intervalDates._2.toString(dateFormat)}"},"includeUnknownCloudCover":true,"maxResults":"${source.pageSize}","startingNumber":"$pageStart","sortOrder":"DESC"}"""

  private def getToken: String = {
    val doc = MongoDAO.getDoc("token", MongoDAO.EARTH_EXPLORER_AUTH_COL).get
    doc.getString("token").getValue
  }

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    val token = getToken

    val tokenUrl = url.replace("<token>", token)
    singleRequest(tokenUrl, workTimeout, process, ErrorHandlers.earthExplorerErrorHandler)
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
    val displayId = (node \ "displayId").as[String]
    new File(s"${source.baseDir}/$displayId").mkdirs()

    setupEntryMongo(displayId)

    generateEEMetadataWork(entityId, displayId)
  }

  private def generateEEMetadataWork(entityId: String, displayId: String) = {
    val token = getToken

    val mdUrl = s"""${source.baseUrl}metadata?jsonRequest={"apiKey":"$token","datasetName":"${source.productType}","entityIds":"$entityId"}"""

    val mdExt = source.extractions.filter(e => e.name == source.metadataExt || e.context == source.metadataExt)

    val processEEMetadata = (responseBytes: Array[Byte]) => {
      processExtractions(responseBytes, mdExt, displayId, mdUrl).right.get

      MongoDAO.addFieldToDoc(displayId, "data", getDataDoc(source.productType, displayId, entityId))
      List()
    }

    if (mdExt.nonEmpty)
      List(
        new ExtractionWork(new ExtractionSource(source.config, source.configName, mdExt, earthExplorerErrorHandler, Some(processEEMetadata)),
          mdUrl, displayId)
      )
    else
      List()
  }


  override def generatePeriodicWork(): EarthExplorerWork = {
    val updatedIngestionWindow = source.adjustWindow(intervalDates)
    source.generateWork(updatedIngestionWindow)
  }

  override def getNextPagesWork(doc: JsValue): Option[Work] = {
    val nextRecord = (doc \ "data" \ "nextRecord").as[JsValue]

    if (nextRecord != JsNull)
      Some(new EarthExplorerWork(source, intervalDates, isEpoch, pageStart + source.pageSize))
    else
      None
  }


}


package sources

import java.io.File
import java.nio.charset.StandardCharsets

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import org.joda.time.DateTime
import org.json.XML
import org.mongodb.scala.bson.BsonDocument
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}
import utils.HTTPClient.singleRequest
import utils.Utils.dateFormat


object EarthExplorer {
  final val configName = "earth-explorer"
  final val PROVIDER = "earth-explorer"
  final val metadataExt = "metadata"
}

class EarthExplorerSource(val config: Config,
                          override val program: String,
                          override val platform: String,
                          override val productType: String)
  extends ProviderPeriodicRESTSource(EarthExplorer.configName, config, program, platform, productType) {
  final val configName = EarthExplorer.configName

  val authConfigOpt: None.type = None
  val epochInitialWork = new EarthExplorerWork(this, ingestionHistoryDates, isEpoch = true)
  val periodicInitialWork = new EarthExplorerWork(this, initialIngestion)
  override val PROVIDER: String = EarthExplorer.PROVIDER
}

class EarthExplorerWork(override val source: EarthExplorerSource,
                        override val ingestionDates: (DateTime, DateTime),
                        override val isEpoch: Boolean = false,
                        override val pageStart: Int = 0)
  extends ProviderPeriodicRESTWork(source, ingestionDates, isEpoch, pageStart) {

  override val url =
    s"""${source.baseUrl}search?jsonRequest={"apiKey":"47e9c35715174f0ab06d4d5a8399383f","datasetName":"${source.productType}","temporalFilter":{"startDate":"${ingestionDates._1.toString(dateFormat)}","endDate":"${ingestionDates._2.toString(dateFormat)}"},"includeUnknownCloudCover":true,"maxResults":"${source.pageSize}","startingNumber":"$pageStart","sortOrder":"DESC"}"""

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    singleRequest(url, source.workTimeout, process, ErrorHandlers.earthExplorerErrorHandler, source.authConfigOpt)
  }

  override def process(responseBytes: Array[Byte]): List[Work] = {
    val docJson = XML.toJSONObject(new String(responseBytes, StandardCharsets.UTF_8)).toString
    val doc = Json.parse(docJson)
    var workToBeDone = List[Work]()

//    getNextPagesWork(doc).foreach(w => workToBeDone ::= w)

    (doc \ "data" \ "results").as[List[JsObject]].foreach(entry => workToBeDone :::= processEntry(entry))

    saveFetchingLog(BsonDocument(docJson))

    workToBeDone
  }

  private def processEntry(node: JsValue) = {

    val entityId = (node \ "entityId").as[String]
    new File(s"data/$entityId").mkdirs() //TODO data harcoded, insert sentinel/sentinel1/product

    setupEntryMongo(entityId)

    generateEEMetadataWork(entityId)
  }

  private def generateEEMetadataWork(entityId: String) = {
    val mdUrl = s"""${source.baseUrl}metadata?jsonRequest={"apiKey":"47e9c35715174f0ab06d4d5a8399383f","datasetName":"${source.productType}","entityIds":"$entityId"}"""

    val mdExt = source.extractions.filter(e => e.name == EarthExplorer.metadataExt || e.context == EarthExplorer.metadataExt)

    if (mdExt.nonEmpty)
      List(
        new ExtractionWork(new ExtractionSource(source.config, source.configName, mdExt, ErrorHandlers.earthExplorerErrorHandler, None), mdUrl, entityId))
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
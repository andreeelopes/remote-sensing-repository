package sources

import java.io.File
import java.nio.charset.StandardCharsets

import akka.actor.{ActorContext, ActorRef}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import mongo.MongoDAO
import org.joda.time.DateTime
import org.json.XML
import org.mongodb.scala.Document
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}
import protocol.worker.WorkExecutor.WorkComplete
import utils.AkkaHTTP
import utils.Utils.{dateFormat, getAllExtractions}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class EarthExplorerSource(val config: Config,
                          val program: String,
                          val platform: String,
                          val productType: String)
  extends PeriodicRESTSource("earth-explorer", config) {

  final val configName = "earth-explorer"
  val authConfigOpt: None.type = None
  val epochInitialWork = new EarthExplorerWork(this, ingestionHistoryDates, isEpoch = true)
  val periodicInitialWork = new EarthExplorerWork(this, initialIngestion)

  val extractions: List[Extraction] = getAllExtractions(config, configName, program, platform, productType)

}

class EarthExplorerWork(override val source: EarthExplorerSource,
                        override val ingestionDates: (DateTime, DateTime),
                        override val isEpoch: Boolean = false,
                        override val pageStart: Int = 0)
  extends PeriodicRESTWork(source, ingestionDates, isEpoch, pageStart) {

  override val url =
    s"""${source.baseUrl}search?jsonRequest={"apiKey":"b5ecbce1f1ea467dba0543dcebafcfe5","datasetName":"${source.productType}","temporalFilter":{"startDate":"${ingestionDates._1.toString(dateFormat)}","endDate":"${ingestionDates._2.toString(dateFormat)}"},"includeUnknownCloudCover":true,"maxResults":"${source.pageSize}","startingNumber":"$pageStart","sortOrder":"DESC"}"""


  def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {

    implicit val origSender: ActorRef = context.sender

    AkkaHTTP.singleRequest(url, source.authConfigOpt).onComplete {
      case Success(response) =>

        Unmarshal(response.entity.withoutSizeLimit).to[Array[Byte]].onComplete {
          case Success(responseBytes) =>

            val workToBeDone = process(responseBytes)

            println(workToBeDone)

            origSender ! WorkComplete(workToBeDone)

          case Failure(e) => throw new Exception(e)
        }
      case Failure(e) => throw new Exception(e)
    }

  }


  private def process(responseBytes: Array[Byte]): List[Work] = {
    val docJson = XML.toJSONObject(new String(responseBytes, StandardCharsets.UTF_8)).toString
    val doc = Json.parse(docJson)
    var workToBeDone = List[Work]()

    //    getNextPagesWork(doc).foreach(w => workToBeDone ::= w)

    (doc \ "data" \ "results").as[List[JsObject]].headOption.foreach(entry => workToBeDone :::= processEntry(entry)) //TODO headopt

    saveFetchingLog(docJson, source.productType, "earth-explorer")

    workToBeDone
  }

  private def processEntry(node: JsValue) = {

    val entityId = (node \ "entityId").as[String]
    new File(s"data/$entityId").mkdirs() //TODO data harcoded, insert sentinel/sentinel1/product

    MongoDAO.insertDoc(
      Document(
        "_id" -> entityId,
        "program" -> source.program,
        "platform" -> source.platform,
        "productType" -> source.productType
      ),
      MongoDAO.COMMON_COL)

    MongoDAO.insertDoc(Document("_id" -> entityId), source.program)
    MongoDAO.insertDoc(Document("_id" -> entityId), source.platform)
    MongoDAO.insertDoc(Document("_id" -> entityId), source.productType)

    generateEEMetadataWork(entityId)

  }

  private def generateEEMetadataWork(entityId: String) = {
    val mdUrl = s"""${source.baseUrl}metadata?jsonRequest={"apiKey":"b5ecbce1f1ea467dba0543dcebafcfe5","datasetName":"${source.productType}","entityIds":"$entityId"}"""

    val mdExt = source.extractions.filter(e => e.name == "metadata" || e.context == "metadata")

    if (mdExt.nonEmpty)
      List(new ExtractionWork(new ExtractionSource(source.config, source.configName, mdExt), mdUrl, entityId))
    else
      List()
  }

  def generatePeriodicWork(): EarthExplorerWork = {
    val updatedIngestionWindow = source.adjustIngestionWindow(ingestionDates)
    new EarthExplorerWork(source, updatedIngestionWindow)
  }

  private def getNextPagesWork(doc: JsValue): Option[Work] = {
    val nextRecord = (doc \ "data" \ "nextRecord").as[JsValue]

    if (nextRecord != JsNull)
      Some(new EarthExplorerWork(source, ingestionDates, isEpoch, pageStart + source.pageSize))
    else
      None
  }

}
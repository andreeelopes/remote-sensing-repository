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
import org.mongodb.scala._
import play.api.libs.json.{JsObject, JsValue, Json}
import protocol.worker.WorkExecutor.WorkComplete
import utils.AkkaHTTP
import utils.ParsingUtils.processExtractions
import utils.Utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class CopernicusOSearchSource(val config: Config,
                              val program: String,
                              val platform: String,
                              val productType: String)
  extends PeriodicRESTSource("copernicus.copernicus-oah-opensearch", config) {

  final val configName = "copernicus.copernicus-oah-opensearch"
  val authConfigOpt = Some(AuthConfig(configName, config))
  val epochInitialWork = new CopernicusOSearchWork(this, ingestionHistoryDates, isEpoch = true)
  val periodicInitialWork = new CopernicusOSearchWork(this, initialIngestion)

  val extractions: List[Extraction] = getAllExtractions(config, configName, program, platform, productType)
}


class CopernicusOSearchWork(override val source: CopernicusOSearchSource,
                            override val ingestionDates: (DateTime, DateTime),
                            override val isEpoch: Boolean = false,
                            override val pageStart: Int = 0)
  extends PeriodicRESTWork(source, ingestionDates, isEpoch, pageStart) {

  override val url: String = s"${source.baseUrl}start=$pageStart&rows=${source.pageSize}&" +
    s"q=endposition:[${ingestionDates._1.toString(dateFormat)}%20TO%20${ingestionDates._2.toString(dateFormat)}]" +
    s"%20AND%20producttype:${source.productType}"


  def generatePeriodicWork(): CopernicusOSearchWork = {
    val updatedIngestionWindow = source.adjustIngestionWindow(ingestionDates)
    new CopernicusOSearchWork(source, updatedIngestionWindow)
  }

  def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {

    implicit val origSender: ActorRef = context.sender

    AkkaHTTP.singleRequest(url, source.authConfigOpt).onComplete {
      case Success(response) =>

        Unmarshal(response.entity.withoutSizeLimit).to[Array[Byte]].onComplete {
          case Success(responseBytes) =>

            val workToBeDone = process(responseBytes)

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

    getNextPagesWork(doc).foreach(w => workToBeDone ::= w)

    (doc \ "feed" \ "entry").as[List[JsObject]].headOption.foreach(entry => workToBeDone :::= processEntry(entry))

    saveFetchingLog(docJson, source.productType, "copernicus")

    workToBeDone
  }


  private def processEntry(node: JsValue) = {

    val productId = (node \ "id").as[String]
    val title = (node \ "title").as[String]
    new File(s"data/$productId").mkdirs() // TODO data harcoded

    MongoDAO.insertDoc(
      Document(
        "_id" -> productId,
        "program"-> source.program,
        "platform" -> source.platform,
        "productType" -> source.productType
      ),
      MongoDAO.COMMON_COL)

    MongoDAO.insertDoc(Document("_id" -> productId), source.program)
    MongoDAO.insertDoc(Document("_id" -> productId), source.platform)
    MongoDAO.insertDoc(Document("_id" -> productId), source.productType)

    val auxExt = source.extractions.map(e => e.copy(contextFormat = "json"))

    processExtractions(node.toString.getBytes(StandardCharsets.UTF_8), auxExt, productId, url)

    generateCreodiasWork(productId, title) :::
      List(new CopernicusManifestWork(
        new CopernicusManifestSource(source.config, source.program, source.platform, source.productType),
        productId,
        title)
      )

  }

  private def generateCreodiasWork(productId: String, title: String) = {
    val creodiasConfigName = "creodias.creodias-odata"
    val creodiasBaseUrl = source.config.getString(s"sources.$creodiasConfigName.base-url")
    val creodiasUrl = s"$creodiasBaseUrl/${source.platform.capitalize}/search.json?maxRecords=1&productIdentifier=%$title%&status=all"

    val creodiasExt =
      getAllExtractions(source.config, creodiasConfigName, source.program, source.platform, source.productType)

    if (creodiasExt.nonEmpty)
      List(new ExtractionWork(new ExtractionSource(source.config, creodiasConfigName, creodiasExt), creodiasUrl, productId))
    else
      List()
  }


  private def getNextPagesWork(doc: JsValue): Option[Work] = {
    val linksPages = (doc \ "feed" \ "link").as[List[JsValue]]
    val linkNextPage = linksPages.filter(link => (link \ "rel").as[String] == "next")

    if (linkNextPage.nonEmpty)
      Some(new CopernicusOSearchWork(source, ingestionDates, isEpoch, pageStart + source.pageSize))
    else
      None
  }


}

package sources

import java.io.File

import akka.actor.ActorContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import org.joda.time.DateTime
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}
import protocol.worker.WorkExecutor.WorkComplete
import utils.AkkaHTTP
import utils.Utils.{dateFormat, getAllExtractions, writeFile}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class EarthExplorerSource(val config: Config,
                          val program: String,
                          val platform: String,
                          val productType: String)
  extends PeriodicRESTSource("earth-explorer", config) {

  final val configName = "earth-explorer"
  val authConfigOpt = None
  val epochInitialWork = new EarthExplorerWork(this, ingestionHistoryDates, isEpoch = true)
  val periodicInitialWork = new EarthExplorerWork(this, initialIngestion)

  val extractions = getAllExtractions(config, configName, program, platform.toLowerCase, productType)

}

class EarthExplorerWork(override val source: EarthExplorerSource,
                        override val ingestionDates: (DateTime, DateTime),
                        override val isEpoch: Boolean = false,
                        override val pageStart: Int = 0)
  extends PeriodicRESTWork(source, ingestionDates, isEpoch, pageStart) {

  override val url =
    s"""${source.baseUrl}search?jsonRequest={"apiKey":"d38f776d53864d8492206d99b81a447c","datasetName":"${source.productType}","temporalFilter":{"startDate":"${ingestionDates._1.toString(dateFormat)}","endDate":"${ingestionDates._2.toString(dateFormat)}"},"includeUnknownCloudCover":true,"maxResults":"${source.pageSize}","startingNumber":"$pageStart","sortOrder":"DESC"}"""


  def execute()(implicit context: ActorContext, mat: ActorMaterializer) = {

    implicit val origSender = context.sender

    AkkaHTTP.singleRequest(url, source.authConfigOpt).onComplete {
      case Success(response) =>

        Unmarshal(response.entity.withoutSizeLimit).to[String].onComplete {
          case Success(responseString) =>

            val workToBeDone = process(responseString)

            println(workToBeDone)

            origSender ! WorkComplete(workToBeDone)

          case Failure(e) => throw new Exception(e)
        }
      case Failure(e) => throw new Exception(e)
    }

  }


  private def process(docJson: String): List[Work] = {
    val doc = Json.parse(docJson)
    var workToBeDone = List[Work]()

    //    getNextPagesWork(doc).foreach(w => workToBeDone ::= w)

    (doc \ "data" \ "results").as[List[JsObject]].headOption.foreach(entry => workToBeDone :::= processEntry(entry)) //TODO headopt

    writeFile(s"data/($pageStart)-(${source.productType})-earth-explorer.json", docJson)
    //    writeFile(s"data/(${ingestionDates._1})-(${ingestionDates._2})-($pageStart)-(${source.productType})-copernicus-osearch.json",docJson)

    workToBeDone
  }

  private def processEntry(node: JsValue) = {

    val entityId = (node \ "entityId").as[String]
    new File(s"data/$entityId").mkdirs() //TODO data harcoded, insert sentinel/sentinel1/product

    generateEEMetadataWork(entityId)

  }

  private def generateEEMetadataWork(entityId: String) = {
    val mdUrl = s"""${source.baseUrl}metadata?jsonRequest={"apiKey":"d38f776d53864d8492206d99b81a447c","datasetName":"${source.productType}","entityIds":"$entityId"}"""

    val mdExt = source.extractions.filter(e => e.name == "metadata" || e.context == "metadata")

    if (mdExt.nonEmpty)
      List(new ExtractionWork(new ExtractionSource(source.config, source.configName, mdExt), mdUrl, entityId))
    else
      List()
  }


  private def getNextPagesWork(doc: JsValue): Option[Work] = {
    val nextRecord = (doc \ "data" \ "nextRecord").as[JsValue]

    if (nextRecord != JsNull)
      Some(new EarthExplorerWork(source, ingestionDates, isEpoch, pageStart + source.pageSize))
    else
      None
  }

  def generatePeriodicWork() = {
    val updatedIngestionWindow = source.adjustIngestionWindow(ingestionDates)
    new EarthExplorerWork(source, updatedIngestionWindow)
  }

}
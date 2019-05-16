package sources.copernicus

import java.io.File

import akka.actor.ActorContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import org.joda.time.DateTime
import org.json.XML
import play.api.libs.json.{JsObject, JsValue, Json}
import protocol.worker.WorkExecutor.WorkComplete
import sources._
import utils.AkkaHTTP
import utils.Utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class CopernicusOSearchSource(val config: Config,
                              val platform: String,
                              val productType: String)
  extends PeriodicRESTSource("copernicus.copernicus-oah-opensearch", config) {

  final val configName = "copernicus.copernicus-oah-opensearch"
  val authConfigOpt = Some(AuthConfig(configName, config))
  val epochInitialWork = new CopernicusOSearchWork(this, ingestionHistoryDates, isEpoch = true)
  val periodicInitialWork = new CopernicusOSearchWork(this, initialIngestion)
}


class CopernicusOSearchWork(override val source: CopernicusOSearchSource,
                            override val ingestionDates: (DateTime, DateTime),
                            override val isEpoch: Boolean = false,
                            override val pageStart: Int = 0)
  extends PeriodicRESTWork(source, ingestionDates, isEpoch, pageStart) {

  //TODO order by new to old, add more parameters
  override val url = s"${source.baseUrl}start=$pageStart&rows=${source.pageSize}&" +
    s"q=ingestiondate:[${ingestionDates._1.toString(dateFormat)}%20TO%20${ingestionDates._2.toString(dateFormat)}]" +
    s"%20AND%20producttype:${source.productType}"


  def generatePeriodicWork() = {
    val updatedIngestionWindow = source.adjustIngestionWindow(ingestionDates)
    new CopernicusOSearchWork(source, updatedIngestionWindow)
  }

  def execute()(implicit context: ActorContext, mat: ActorMaterializer) = {

    implicit val origSender = context.sender

    AkkaHTTP.singleRequest(url, source.authConfigOpt).onComplete {
      case Success(response) =>

        Unmarshal(response.entity.withoutSizeLimit).to[String].onComplete {
          case Success(responseString) =>

            val workToBeDone = process(XML.toJSONObject(responseString).toString(3))

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

    (doc \ "feed" \ "entry").as[List[JsObject]].headOption.foreach(entry => workToBeDone :::= processEntry(entry))

    writeFile(s"data/($pageStart)-(${source.productType})-copernicus-osearch", docJson)
    //    writeFile(s"data/(${ingestionDates._1})-(${ingestionDates._2})-($pageStart)-(${source.productType})-copernicus-osearch",compact(render(doc)))

    workToBeDone
  }

  private def processEntry(node: JsValue) = {

    val productId = (node \ "id").as[String]
    val title = (node \ "title").as[String]
    new File(s"data/$productId").mkdirs() //TODO data harcoded

    List(
      new CopernicusManifestWork(
        new CopernicusManifestSource(source.config, source.platform, source.productType),
        productId,
        title
      ),

      new CreodiasMDWork(new CreodiasMDSource(source.config), productId, title, source.platform)
    )
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

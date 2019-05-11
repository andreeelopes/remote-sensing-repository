package sources.copernicus

import java.io.File

import akka.actor.ActorContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import org.joda.time.DateTime
import protocol.worker.WorkExecutor.WorkComplete
import sources._
import utils.AkkaHTTP
import utils.Utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.xml.{Elem, Node, XML}

class CopernicusOSearchSource(val config: Config,
                              val platform: String = "Sentinel2",
                              val productType: String = "S2MSI1C")
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

        Unmarshal(response.entity).to[String].onComplete {
          case Success(responseString) =>

            val workToBeDone = process(XML.loadString(responseString))

            origSender ! WorkComplete(workToBeDone)

          case Failure(e) => throw new Exception(e)
        }
      case Failure(e) => throw new Exception(e)
    }

  }

  private def process(doc: Elem): List[Work] = {
    var workToBeDone = List[Work]()

    //    getNextPagesWork(doc).foreach(w => workToBeDone ::= w)

    (doc \ "entry").head.foreach(node => workToBeDone :::= processEntry(node))

    XML.save(s"data/$pageStart-copernicus-osearch", doc)
    //    XML.save(s"(${ingestionDates._1})-(${ingestionDates._2})-($pageStart)-copernicus-osearch", doc)

    workToBeDone
  }

  private def processEntry(node: Node) = {
    val productId = (node \ "id").text
    val title = (node \ "title").text
    new File(s"data/$productId").mkdirs() //TODO data harcoded

    //TODO what about other sentinels...
    List(
      new CopernicusManifestWork(new CopernicusManifestSource(source.config), productId, title),
      new CreodiasMDWork(new CreodiasMDSource(source.config), productId, title, source.platform)
    )

  }


  private def getNextPagesWork(doc: Node): Option[Work] = {
    val linksPages = doc.child.filter(node => node.label.equals("link"))
    val linkNextPage = linksPages.filter(node => (node \@ "rel").equals("next"))

    if (linkNextPage.nonEmpty)
      Some(new CopernicusOSearchWork(source, ingestionDates, isEpoch, pageStart + source.pageSize))
    else
      None
  }


}

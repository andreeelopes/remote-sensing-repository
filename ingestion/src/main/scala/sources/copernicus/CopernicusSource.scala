package sources.copernicus

import java.io.{File, PrintWriter}

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
import scala.xml.{Elem, XML}


class CopernicusMDSource(val config: Config) extends PeriodicRESTSource("copernicusOAH", config) {

  final val configName = "copernicusOAH"

  val authConfigOpt = Some(AuthConfig(configName, config))
  val epochInitialWork = new CopernicusMDWork(this, ingestionHistoryDates, isEpoch = true)
  val periodicInitialWork = new CopernicusMDWork(this, initialIngestion)

}


class CopernicusMDWork(override val source: CopernicusMDSource,
                       override val ingestionDates: (DateTime, DateTime),
                       override val isEpoch: Boolean = false,
                       override val pageStart: Int = 0)
  extends PeriodicRESTWork(source, ingestionDates, isEpoch, pageStart) {

  //TODO order by new to old, add more parameters
  override val url = s"${source.baseUrl}start=$pageStart&rows=${source.pageSize}&" +
    s"q=ingestiondate:[${ingestionDates._1.toString(dateFormat)}%20TO%20${ingestionDates._2.toString(dateFormat)}]" +
    s"%20AND%20platformname:Sentinel-2"


  var productId: String = _
  var resource: Elem = _

  def generatePeriodicWork() = {
    val updatedIngestionWindow = source.adjustIngestionWindow(ingestionDates)
    new CopernicusMDWork(source, updatedIngestionWindow)
  }

  def execute()(implicit context: ActorContext, mat: ActorMaterializer) = {

    implicit val origSender = context.sender

    AkkaHTTP.singleRequest(url, source.authConfigOpt).onComplete {
      case Success(response) =>

        Unmarshal(response.entity).to[String].onComplete {
          case Success(responseString) =>
            resource = XML.loadString(responseString)

            val workToBeDone = getGeneratedWork

            val processedXML = resource // TODO preProcess()

            origSender ! WorkComplete(workToBeDone)


          case Failure(e) => throw new Exception(e)
        }
      case Failure(e) => throw new Exception(e)
    }

  }

  def getGeneratedWork: List[Work] = {
    var workToBeDone = List[Work]()

    // Get next page

    val linksPages = resource.child.filter(node => node.label.equals("link"))
    val linkNextPage = linksPages.filter(node => (node \@ "rel").equals("next"))

    if (linkNextPage.nonEmpty) {
      //      val linkLast = linksPages.filter(node => (node \@ "rel").equals("last")).head
      //      var last = linkLast \@ "href"
      //      last = last.replaceAll(" ", "%20")
      //
      //      val lastPageStart = Uri.parseAbsolute(last).query().get("start").get.toInt
      //
      //      for (pageStart <- source.pageSize to lastPageStart by source.pageSize)
      //            workToBeDone ::= generateNextPagesWork()TODO
    }


    //    // TODO get more info
    //    (resource \ "entry").foreach { node =>
    //
    //      productId = (node \ "id").text
    //      val links = node \ "link"
    //      val linkAlternative = links.filter(node => (node \@ "rel").equals("alternative"))
    //      val href = linkAlternative \@ "href"
    //
    //      new File(productId).mkdirs
    //      new PrintWriter(s"$productId/opensearchMD.xml") {
    //        try write(node.toString) finally close()
    //      }
    //
    //      workToBeDone ::= new CopernicusODataMDWork(new CopernicusODataMDSource(source.config), productId)
    //      workToBeDone ::= new CopernicusODataFileWork(new CopernicusODataFileSource(source.config), s"${href}Nodes", productId)
    //    }

    val node = (resource \ "entry").head

    productId = (node \ "id").text
    val links = node \ "link"
    val linkAlternative = links.filter(node => (node \@ "rel").equals("alternative"))
    val href = linkAlternative \@ "href"

    new File(productId).mkdirs
    new PrintWriter(s"$productId/opensearchMD.xml") {
      try write(node.toString) finally close()
    }

    workToBeDone ::= new CopernicusODataMDWork(new CopernicusODataMDSource(source.config), productId)
    workToBeDone ::= new CopernicusODataFileWork(new CopernicusODataFileSource(source.config), s"${href}Nodes", productId)


    workToBeDone
  }

  def generateNextPagesWork() = new CopernicusMDWork(source, ingestionDates, isEpoch, pageStart + source.pageSize)

  def preProcess() = {} //TODO


}

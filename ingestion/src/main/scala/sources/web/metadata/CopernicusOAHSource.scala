package sources.web.metadata


import java.io.PrintWriter

import akka.actor.ActorContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import org.joda.time.DateTime
import protocol.worker.WorkExecutor.WorkComplete
import sources.{AuthConfig, PeriodicComponent, PeriodicConfig, Work}
import utils.Utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.xml.{Elem, XML}


class CopernicusOAHSource(config: Config) extends RESTSource("copernicusOAH", config)
  with PeriodicComponent {

  final val configName = "copernicusOAH"

  val periodicConfig = PeriodicConfig(configName, config)
  val authConfigOpt = Some(AuthConfig(configName, config))
  val epochInitialWork = new CopernicusOAHWork(this, periodicConfig.ingestionHistoryDates, isEpoch = true)
  val initialWork = new CopernicusOAHWork(this, periodicConfig.initialIngestion)

}


class CopernicusOAHWork(override val source: CopernicusOAHSource,
                        override val ingestionDates: (DateTime, DateTime),
                        override val isEpoch: Boolean = false,
                        override val pageStart: Int = 0)

  extends RESTWork(source, ingestionDates, isEpoch, pageStart) {

  //TODO order by new to old, add more parameters
  override val url = s"${source.baseUrl}start=$pageStart&rows=${source.periodicConfig.pageSize}&q=ingestiondate:" +
    s"[${ingestionDates._1.toString(dateFormat)}%20TO%20${ingestionDates._2.toString(dateFormat)}]"

  var resource: Elem = _

  def execute()(implicit context: ActorContext, mat: ActorMaterializer) = {
    println(s"Starting to fetch: $url") // TODO logs

    implicit val origSender = context.sender

    httpRequest.onComplete {
      case Success(response) =>

        Unmarshal(response.entity).to[String].onComplete {
          case Success(responseString) =>
            resource = XML.loadString(responseString)

            val processedXML = preProcess()

            new PrintWriter(s"$ingestionDates-Copernicus-metadata$workId.xml") {
              try write(processedXML.toString) finally close()
            }

            val workToBeDone = getNextWork
            origSender ! WorkComplete(workToBeDone)


          case Failure(e) => throw new Exception(e)
        }
      case Failure(e) => throw new Exception(e)
    }

  }


  def preProcess() = resource = resource

  def getNextWork: List[Work] = {
    var workToBeDone = List[CopernicusOAHWork]()

    val links = resource.child.filter(node => node.label.equals("link"))
    val linkNext = links.filter(node => (node \@ "rel").equals("next"))

    if (linkNext.nonEmpty) {

      //      val linkLast = links.filter(node => (node \@ "rel").equals("last")).head
      //      var last = linkLast \@ "href"
      //      last = last.replaceAll(" ", "%20")
      //
      //      val lastPageStart = Uri.parseAbsolute(last).query().get("start").get.toInt
      //
      //      for (pageStart <- source.pageSize to lastPageStart by source.pageSize)

      workToBeDone ::= generateNextResourceWork()
    }

    workToBeDone
  }

  def generateNextResourceWork() =
    new CopernicusOAHWork(source, ingestionDates, isEpoch, pageStart + source.periodicConfig.pageSize)


  def generatePeriodicWork() = {
    val updatedIngestionWindow = source.adjustIngestionWindow(ingestionDates)

    new CopernicusOAHWork(source, updatedIngestionWindow, isEpoch)
  }


}

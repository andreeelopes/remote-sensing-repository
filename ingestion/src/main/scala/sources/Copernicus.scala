package sources


import java.io.PrintWriter

import akka.actor.{ActorRef, Scheduler}
import akka.http.scaladsl.model.{HttpResponse, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import commons.Work
import org.joda.time.DateTime
import utils.Utils._
import worker.WorkExecutor.WorkComplete

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.xml.XML


class CopernicusSource(config: Config) extends Source("copernicusOAH", config) {

  override val initialWork = new CopernicusWork(this, initialIngestion)

  override val epochInitialWork = new CopernicusWork(this, ingestionHistoryDates, isEpoch = true)


  override def generateQuery(pageStart: Int, ingestionDates: (DateTime, DateTime)) = {
    s"${baseUrl}start=$pageStart&rows=$pageSize&q=ingestiondate:" +
      s"[${ingestionDates._1.toString(dateFormat)}%20TO%20${ingestionDates._2.toString(dateFormat)}]"
  }


  override def generateWork(prevWork: Work, isRecursive: Boolean = false) = {
    if (isRecursive)
      new CopernicusWork(prevWork.source, prevWork.ingestionDates, prevWork.isEpoch, prevWork.pageStart + pageSize)
    else {
      val updatedIngestionWindow = adjustIngestionWindow(prevWork.ingestionDates)

      new CopernicusWork(prevWork.source, updatedIngestionWindow, prevWork.isEpoch)
    }
  }

}


class CopernicusWork(override val source: Source,
                     override val ingestionDates: (DateTime, DateTime),
                     override val isEpoch: Boolean = false,
                     override val pageStart: Int = 0)
  extends Work(source, ingestionDates, isEpoch, pageStart) {

  override def unmarshal(response: HttpResponse)(implicit actorMat: ActorMaterializer, origSender: ActorRef) = {

    Unmarshal(response.entity).to[String].onComplete {

      case Success(value) =>

        val processedXML = preProcess(value)

        new PrintWriter(s"${ingestionDates}metadata$workId.xml") {
          try write(processedXML.toString) finally close()
        }

        origSender ! WorkComplete(getNext(value))

      case Failure(e) => throw new Exception(e.getMessage)
    }

  }


  override def preProcess(metadata: String) = metadata

  override def getNext(metadata: String): List[Work] = {
    var workToBeDone = List[Work]()

    val xmlElem = XML.loadString(metadata)

    val links = xmlElem.child.filter(node => node.label.equals("link"))
    val linkNext = links.filter(node => (node \@ "rel").equals("next"))

    if (linkNext.nonEmpty) {

      //      val linkLast = links.filter(node => (node \@ "rel").equals("last")).head
      //      var last = linkLast \@ "href"
      //      last = last.replaceAll(" ", "%20")
      //
      //      val lastPageStart = Uri.parseAbsolute(last).query().get("start").get.toInt
      //
      //      for (pageStart <- source.pageSize to lastPageStart by source.pageSize)

      workToBeDone ::= source.generateWork(this, isRecursive = true)
    }

    workToBeDone
  }

}
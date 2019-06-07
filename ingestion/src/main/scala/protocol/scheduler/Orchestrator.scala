package protocol.scheduler


import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import protocol.master.{Master, MasterSingleton}
import sources.{CopernicusOSearchSource, EarthExplorerSource, Work}
import utils.Utils.productsToFetch

object Orchestrator {

  def props: Props = Props(new Orchestrator)


  trait TriggerMsg {
    val work: Work
  }

  case class NotOk(work: Work) extends TriggerMsg

  case class ProduceWork(work: Work) extends TriggerMsg

  case class Retry(work: Work) extends TriggerMsg

}

class Orchestrator extends Actor with ActorLogging {

  import Orchestrator._
  import context._

  private val scheduler = context.system.scheduler
  private val config = context.system.settings.config
  implicit val mat: ActorMaterializer = ActorMaterializer()(context)

  private val masterProxy = context.actorOf(
    MasterSingleton.proxyProps(context.system),
    name = "masterProxy")

  productsToFetch(config, "copernicus.copernicus-oah-opensearch").foreach { p =>
    p.productType.foreach { pt =>
      val copernicus = new CopernicusOSearchSource(config, p.program, p.platform, pt)
      copernicus.start
    }
  }

  productsToFetch(config, "earth-explorer").foreach { p =>
    p.productType.foreach { pt =>
      val earthExplorer = new EarthExplorerSource(config, p.program, p.platform, pt)
      earthExplorer.start
    }
  }

  def receive = {

    case ProduceWork(work) =>
      log.info("Produced work")
      sendWork(work)

    case Master.Ack(work) =>
      log.info("Got ack for workId {}", work.workId)

    //      //TODO possible problems because of timers
    //      work match {
    //        case pw: PeriodicWork =>
    //          if (!pw.isEpoch)
    //            scheduler.scheduleOnce(pw.source.fetchingFrequency, self, ProduceWork(pw.generatePeriodicWork()))
    //      }

    case NotOk(work) =>
      log.info("Work {} not accepted, retry after a while", work.workId)
      scheduler.scheduleOnce(work.source.retryInterval, self, Retry(work))

    case Retry(work) =>
      log.info("Retrying work {}", work.workId)
      sendWork(work)
  }


  def sendWork(work: Work): Unit = {
    implicit val timeout: Timeout = Timeout(work.source.retryTimeout)
    (masterProxy ? work).recover {
      case _ => NotOk(work)
    } pipeTo self
  }

}


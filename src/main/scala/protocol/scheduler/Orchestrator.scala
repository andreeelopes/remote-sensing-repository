package protocol.scheduler


import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import protocol.master.{Master, MasterSingleton}
import sources.{CopernicusOSearch, CopernicusOSearchSource, EarthExplorer, EarthExplorerSource, PeriodicWork, Work}
import utils.Utils.productsToFetch

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

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

  private val config = context.system.settings.config

  val retryInterval: FiniteDuration = config.getDuration(s"orchestrator.retry-interval").getSeconds.seconds
  val retryTimeout: FiniteDuration = config.getDuration(s"orchestrator.retry-timeout").getSeconds.seconds

  private val scheduler = context.system.scheduler
  implicit val mat: ActorMaterializer = ActorMaterializer()(context)

  private val masterProxy = context.actorOf(
    MasterSingleton.proxyProps(context.system),
    name = "masterProxy")


  productsToFetch(config, CopernicusOSearch.configName).foreach { p =>
    p.productType.foreach { pt =>
      val copernicus = new CopernicusOSearchSource(config, p.program, p.platform, pt)
      copernicus.start
    }
  }

  productsToFetch(config, EarthExplorer.configName).foreach { p =>
    p.productType.foreach { pt =>
      val earthExplorer = new EarthExplorerSource(config, p.program, p.platform, pt)
      earthExplorer.start
    }

  }

  def receive: Receive = {

    case ProduceWork(work) =>
      log.info(s"Produced work - $work")
      sendWork(work)

    case Master.Ack(work) =>
      log.info("Got ack for workId {}", work.workId)

      work match {
        case pw: PeriodicWork =>
          if (!pw.isEpoch)
            scheduler.scheduleOnce(pw.source.fetchingFrequency, self, ProduceWork(pw.generatePeriodicWork()))
      }

    case NotOk(work) =>
      log.info("Work {} not accepted, retry after a while", work.workId)
      scheduler.scheduleOnce(retryInterval, self, Retry(work))

    case Retry(work) =>
      log.info("Retrying work {}", work.workId)
      sendWork(work)
  }


  def sendWork(work: Work): Unit = {
    implicit val timeout: Timeout = Timeout(retryTimeout)
    (masterProxy ? work).recover {
      case _ => NotOk(work)
    } pipeTo self
  }

}


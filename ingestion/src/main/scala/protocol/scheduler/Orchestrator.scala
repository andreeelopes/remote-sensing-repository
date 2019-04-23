package protocol.scheduler


import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import protocol.master.{Master, MasterSingleton}
import sources.Work
import sources.periodic.{CopernicusMDSource, PeriodicWork}

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

  val scheduler = context.system.scheduler
  val config = context.system.settings.config
  implicit val mat = ActorMaterializer()(context)

  val masterProxy = context.actorOf(
    MasterSingleton.proxyProps(context.system),
    name = "masterProxy")

  val copernicus = new CopernicusMDSource(config)
  copernicus.start

  def receive = {

    case ProduceWork(work) =>
      log.info("Produced work")
      sendWork(work)

    case Master.Ack(work) =>
      log.info("Got ack for workId {}", work.workId)

      //TODO possible problems because of timers
      work match {
        case pw: PeriodicWork =>
          if (!pw.isEpoch)
            scheduler.scheduleOnce(pw.source.fetchingFrequency, self, ProduceWork(pw.generatePeriodicWork()))
      }

    case NotOk(work) =>
      log.info("Work {} not accepted, retry after a while", work.workId)
      scheduler.scheduleOnce(work.source.retryInterval, self, Retry(work))

    case Retry(work) =>
      log.info("Retrying work {}", work.workId)
      sendWork(work)
  }


  def sendWork(work: Work): Unit = {
    implicit val timeout = Timeout(work.source.retryTimeout)
    (masterProxy ? work).recover {
      case _ => NotOk(work)
    } pipeTo self
  }

}


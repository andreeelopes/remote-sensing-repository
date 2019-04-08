package scheduler


import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern._
import akka.util.Timeout
import commons.Work
import master.{Master, MasterSingleton}
import sources.{CopernicusSource, Source}
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

  implicit val scheduler = context.system.scheduler
  val config = context.system.settings.config

  val masterProxy = context.actorOf(
    MasterSingleton.proxyProps(context.system),
    name = "masterProxy")

  var workCounter = 0

  val copernicus = new CopernicusSource(config)
  Source.start(copernicus)

  def receive = {

    case ProduceWork(work) =>
      workCounter += 1
      log.info("Produced work: {}", workCounter)
      sendWork(work)

    case Master.Ack(work) =>
      log.info("Got ack for workId {}", work.workId)

      if (!work.isEpoch)
        Source.scheduleOnce(ProduceWork(work.source.generateWork(work)))

    case NotOk(work) =>
      log.info("commons.Work {} not accepted, retry after a while", work.workId)
      Source.scheduleOnce(Retry(work))

    case Retry(work) =>
      log.info("Retrying work {}", work.workId)
      sendWork(work)
  }


  def sendWork(work: Work): Unit = {
    implicit val timeout = Timeout(copernicus.timeout)
    (masterProxy ? work).recover {
      case _ => NotOk(work)
    } pipeTo self
  }

}


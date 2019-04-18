package protocol.scheduler


import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import protocol.master.{Master, MasterSingleton}
import sources.web.metadata.{CopernicusOAHSource, RESTWork}
import sources.{PeriodicSource, Work}

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
  implicit val mat = ActorMaterializer()(context)

  val masterProxy = context.actorOf(
    MasterSingleton.proxyProps(context.system),
    name = "masterProxy")

  val copernicus1 = new CopernicusOAHSource(config)
  PeriodicSource.start(copernicus1)

  def receive = {

    case ProduceWork(work) =>
      log.info("Produced work")
      sendWork(work)

    case Master.Ack(work) =>
      log.info("Got ack for workId {}", work.workId)

      work match {
        case rw: RESTWork => // TODO not only rest work
          if (!rw.isEpoch)
            PeriodicSource.scheduleOnceSource(ProduceWork(rw.generatePeriodicWork()))
      }


    case NotOk(work) =>
      log.info("Work {} not accepted, retry after a while", work.workId)
      PeriodicSource.scheduleOnceSource(Retry(work))

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


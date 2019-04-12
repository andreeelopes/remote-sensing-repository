package protocol.scheduler


import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern._
import akka.util.Timeout
import work.Work
import protocol.master.{Master, MasterSingleton}
import sources.{CopernicusSource, Source}


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

  val copernicus1 = new CopernicusSource(config)
  Source.start(copernicus1)
  //  val copernicus2 = new CopernicusSource(config)
  //  Source.start(copernicus2)
  //  val copernicus3 = new CopernicusSource(config)
  //  Source.start(copernicus3)
  //  val copernicus4 = new CopernicusSource(config)
  //  Source.start(copernicus4)
  //  val copernicus5 = new CopernicusSource(config)
  //  Source.start(copernicus5)
  //  val copernicus7 = new CopernicusSource(config)
  //  Source.start(copernicus7)
  //  val copernicus8 = new CopernicusSource(config)
  //  Source.start(copernicus8)
  //  val copernicus9 = new CopernicusSource(config)
  //  Source.start(copernicus9)
  //  val copernicus10 = new CopernicusSource(config)
  //  Source.start(copernicus10)
  //  val copernicus12 = new CopernicusSource(config)
  //  Source.start(copernicus12)
  //  val copernicus13 = new CopernicusSource(config)
  //  Source.start(copernicus13)
  //  val copernicus14 = new CopernicusSource(config)
  //  Source.start(copernicus14)
  //  val copernicus15 = new CopernicusSource(config)
  //  Source.start(copernicus15)


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
      log.info("Work {} not accepted, retry after a while", work.workId)
      Source.scheduleOnce(Retry(work))

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


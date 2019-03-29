package sources

import akka.actor.{ActorContext, ActorRef, Scheduler}
import commons.Work
import scheduler.Orchestrator.{ProduceWork, Retry, TriggerMsg}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Source {

  def scheduleOnce(msg: TriggerMsg)
                  (implicit self: ActorRef, scheduler: Scheduler) = {

    val scheduleTime = msg match {
      case _: Retry => msg.work.source.retryFrequency
      case _: ProduceWork => msg.work.source.fetchingFrequency
    }

    scheduler.scheduleOnce(scheduleTime, self, msg)
  }

}


abstract class Source(configName: String)(implicit context: ActorContext) {

  import Source._

  val system = context.system
  implicit val scheduler = system.scheduler
  implicit val self = context.self

  val conf = system.settings.config

  val fetchingFrequency = conf.getDuration(s"$configName.fetching-frequency").getSeconds.seconds
  val retryFrequency = conf.getDuration(s"$configName.retry-frequency").getSeconds.seconds
  val timeout = conf.getDuration(s"$configName.timeout").getSeconds.seconds

  val url = conf.getString(s"$configName.url")

  scheduleOnce(ProduceWork(new CopernicusWork(this)))

  def generateWork(): Work

}






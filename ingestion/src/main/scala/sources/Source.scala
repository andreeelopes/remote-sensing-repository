package sources

import akka.actor.{ActorRef, Scheduler}
import com.typesafe.config.Config
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


//@SerialVersionUID(2L)
class Source(configName: String, config: Config)  extends Serializable {

  val fetchingFrequency = config.getDuration(s"$configName.fetching-frequency").getSeconds.seconds
  val retryFrequency = config.getDuration(s"$configName.retry-frequency").getSeconds.seconds
  val timeout = config.getDuration(s"$configName.timeout").getSeconds.seconds

  val url = config.getString(s"$configName.url")

  def generateWork() = new Work(this)

}






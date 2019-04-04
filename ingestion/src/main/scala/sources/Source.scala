package sources

import akka.actor.{ActorRef, Scheduler}
import com.typesafe.config.Config
import commons.Work
import org.joda.time.DateTime
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


class Source(configName: String, config: Config) extends Serializable {

  val fetchingFrequency = config.getDuration(s"$configName.fetching-frequency").getSeconds.seconds
  val retryFrequency = config.getDuration(s"$configName.retry-frequency").getSeconds.seconds
  val timeout = config.getDuration(s"$configName.timeout").getSeconds.seconds
  val epoch = config.getString(s"$configName.epoch").toInt

  var baseUrl = config.getString(s"$configName.base-url")

  var url = "" // completed when calling generateQuery

  val username = config.getString(s"$configName.credentials.username")
  val password = config.getString(s"$configName.credentials.pwd")

  val pageSize = 100
  var pageStart = 0

  var ingestionEnd = new DateTime()
  var ingestionStart = ingestionEnd.minus(fetchingFrequency.toMillis)

  def advanceIngestionWindow() = {
    ingestionEnd = ingestionEnd.plus(fetchingFrequency.toMillis)
    ingestionStart = ingestionStart.plus(fetchingFrequency.toMillis)
  }

  def generateWork() = new Work(this)

}






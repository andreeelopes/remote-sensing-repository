package sources

import akka.actor.{ActorRef, Scheduler}
import com.typesafe.config.Config
import commons.{KryoSerializable, Work}
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


  def start(source: Source)(implicit scheduler: Scheduler, self: ActorRef) = {
    scheduler.scheduleOnce(source.startDelay, self, ProduceWork(source.initialWork))
    scheduler.scheduleOnce(source.startDelay, self, ProduceWork(source.epochInitialWork))
  }

}


class Source(configName: String, config: Config) extends KryoSerializable {

  val username = config.getString(s"$configName.credentials.username")
  val password = config.getString(s"$configName.credentials.pwd")

  val fetchingFrequency = config.getDuration(s"$configName.fetching-frequency").getSeconds.seconds
  val retryFrequency = config.getDuration(s"$configName.retry-frequency").getSeconds.seconds
  val timeout = config.getDuration(s"$configName.timeout").getSeconds.seconds
  val startDelay = config.getDuration(s"$configName.start-delay").getSeconds.seconds
  val epoch = config.getString(s"$configName.epoch").toInt

  val baseUrl = config.getString(s"$configName.base-url")

  val pageSize = 100

  val ingestionHistoryEnd = new DateTime()
  val ingestionHistoryDates = (ingestionHistoryEnd.minusYears(epoch), ingestionHistoryEnd)

  val initialIngestion = (ingestionHistoryEnd, ingestionHistoryEnd.plus(fetchingFrequency.toMillis))

  val initialWork = new Work(this, initialIngestion)
  val epochInitialWork = new Work(this, initialIngestion, isEpoch = true)


  def adjustIngestionWindow(ingestionDates: (DateTime, DateTime)) = {
    (ingestionDates._1.plus(fetchingFrequency.toMillis), ingestionDates._2.plus(fetchingFrequency.toMillis))
  }


  def generateWork(prevWork: Work, isRecursive: Boolean = false): Work = null


  def generateQuery(pageStart: Int, ingestionDates: (DateTime, DateTime)) = ""


}






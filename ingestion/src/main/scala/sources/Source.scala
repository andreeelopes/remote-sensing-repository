package sources

import akka.actor.{ActorRef, Scheduler}
import com.typesafe.config.Config
import work.{KryoSerializable, Work}
import org.joda.time.DateTime
import protocol.scheduler.Orchestrator.{ProduceWork, Retry, TriggerMsg}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Source {

  def scheduleOnce(msg: TriggerMsg)
                  (implicit self: ActorRef, scheduler: Scheduler) = {

    val scheduleTime = msg match {
      case _: Retry => msg.work.source.retryInterval
      case _: ProduceWork => msg.work.source.fetchingFrequency
    }

    scheduler.scheduleOnce(scheduleTime, self, msg)
  }


  def start(source: Source)(implicit scheduler: Scheduler, self: ActorRef) = {
    scheduler.scheduleOnce(source.startDelay, self, ProduceWork(source.initialWork))
    scheduler.scheduleOnce(source.startDelay, self, ProduceWork(source.epochInitialWork))
  }

}


abstract class Source(configName: String, config: Config) extends KryoSerializable {

  val username = config.getString(s"sources.$configName.credentials.username")
  val password = config.getString(s"sources.$configName.credentials.pwd")

  val fetchingFrequency = config.getDuration(s"sources.$configName.fetching-frequency").getSeconds.seconds
  val workTimeout = config.getDuration(s"sources.$configName.work-timeout").getSeconds.seconds
  val retryInterval = config.getDuration(s"sources.$configName.retry-interval").getSeconds.seconds
  val retryTimeout = config.getDuration(s"sources.$configName.retry-timeout").getSeconds.seconds
  val startDelay = config.getDuration(s"sources.$configName.start-delay").getSeconds.seconds
  val epoch = config.getString(s"sources.$configName.epoch").toInt

  val baseUrl = config.getString(s"sources.$configName.base-url")

  val pageSize = config.getInt(s"sources.$configName.page-size")

  val ingestionHistoryEnd = new DateTime()
  val ingestionHistoryDates = (ingestionHistoryEnd.minusYears(epoch), ingestionHistoryEnd)

  val initialIngestion = (ingestionHistoryEnd, ingestionHistoryEnd.plus(fetchingFrequency.toMillis))

  val initialWork: Work
  val epochInitialWork: Work


  def adjustIngestionWindow(ingestionDates: (DateTime, DateTime)) = {
    (ingestionDates._1.plus(fetchingFrequency.toMillis), ingestionDates._2.plus(fetchingFrequency.toMillis))
  }


  def generateWork(prevWork: Work, isRecursive: Boolean = false): Work


  def generateQuery(pageStart: Int, ingestionDates: (DateTime, DateTime)): String


}






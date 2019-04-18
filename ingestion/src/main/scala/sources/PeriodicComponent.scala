package sources

import akka.actor.{ActorRef, Scheduler}
import com.typesafe.config.Config
import org.joda.time.DateTime
import protocol.scheduler.Orchestrator.{ProduceWork, Retry, TriggerMsg}
import sources.web.metadata.{RESTSource, RESTWork}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object PeriodicSource {

  def scheduleOnceSource(msg: TriggerMsg)
                        (implicit self: ActorRef, scheduler: Scheduler) = {


    val work = msg.work.asInstanceOf[RESTWork]

    val scheduleTime = msg match {
      case _: Retry => work.source.retryInterval
      case _: ProduceWork => work.source.periodicConfig.fetchingFrequency
    }

    scheduler.scheduleOnce(scheduleTime, self, msg)
  }


  def start(source: RESTSource)(implicit scheduler: Scheduler, self: ActorRef) = {
    scheduler.scheduleOnce(source.startDelay, self, ProduceWork(source.initialWork))
    scheduler.scheduleOnce(source.startDelay, self, ProduceWork(source.epochInitialWork))
  }

}


trait PeriodicComponent {

  val periodicConfig: PeriodicConfig

  def adjustIngestionWindow(ingestionDates: (DateTime, DateTime)) = {
    (ingestionDates._1.plus(periodicConfig.fetchingFrequency.toMillis),
      ingestionDates._2.plus(periodicConfig.fetchingFrequency.toMillis))
  }

  val epochInitialWork: Work

}

case class PeriodicConfig(sourceName: String, config: Config) {

  val pageSize = config.getInt(s"sources.$sourceName.page-size")
  val fetchingFrequency = config.getDuration(s"sources.$sourceName.fetching-frequency").getSeconds.seconds
  val epoch = config.getString(s"sources.$sourceName.epoch").toInt

  val ingestionHistoryEnd = new DateTime()
  val ingestionHistoryDates = (ingestionHistoryEnd.minusYears(epoch), ingestionHistoryEnd)
  val initialIngestion = (ingestionHistoryEnd, ingestionHistoryEnd.plus(fetchingFrequency.toMillis))
}

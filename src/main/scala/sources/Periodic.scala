package sources

import akka.actor.{ActorContext, Cancellable}
import com.typesafe.config.Config
import org.joda.time.DateTime
import protocol.scheduler.Orchestrator.ProduceWork

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

abstract class PeriodicSource(configName: String, config: Config) extends Source(configName, config) {

  val fetchingFrequency: FiniteDuration = config.getDuration(s"sources.$configName.fetching-frequency").getSeconds.seconds
  val epoch: Int = config.getString(s"sources.$configName.epoch").toInt
  val startDelay: FiniteDuration = config.getDuration(s"sources.$configName.start-delay").getSeconds.seconds

  val ingestionHistoryEnd: DateTime = new DateTime().minusYears(1) //TODO
  val ingestionHistoryDates: (DateTime, DateTime) = (ingestionHistoryEnd.minusYears(epoch), ingestionHistoryEnd)
  val initialIngestion: (DateTime, DateTime) = (ingestionHistoryEnd, ingestionHistoryEnd.plus(fetchingFrequency.toMillis))
  val epochInitialWork: Work
  val periodicInitialWork: PeriodicWork


  def start(implicit context: ActorContext): Cancellable = {
    context.system.scheduler.scheduleOnce(startDelay, context.self, ProduceWork(epochInitialWork))
    //    context.system.scheduler.scheduleOnce(startDelay, context.self, ProduceWork(periodicInitialWork))
  }

  def adjustIngestionWindow(ingestionDates: (DateTime, DateTime)): (DateTime, DateTime) =
    (ingestionDates._1.plus(fetchingFrequency.toMillis), ingestionDates._2.plus(fetchingFrequency.toMillis))

}

abstract class PeriodicWork(override val source: PeriodicSource,
                            val ingestionDates: (DateTime, DateTime),
                            val isEpoch: Boolean = false) extends Work(source) {

  def generatePeriodicWork(): PeriodicWork


}
package sources

import akka.actor.ActorContext
import com.typesafe.config.Config
import org.joda.time.DateTime
import protocol.scheduler.Orchestrator.ProduceWork

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

abstract class PeriodicSource(configName: String, config: Config) extends Source(configName, config) {

  val fetchingFrequency = config.getDuration(s"sources.$configName.fetching-frequency").getSeconds.seconds
  val epoch = config.getString(s"sources.$configName.epoch").toInt
  val startDelay = config.getDuration(s"sources.$configName.start-delay").getSeconds.seconds

  val ingestionHistoryEnd = new DateTime()
  val ingestionHistoryDates = (ingestionHistoryEnd.minusYears(epoch), ingestionHistoryEnd)
  val initialIngestion = (ingestionHistoryEnd, ingestionHistoryEnd.plus(fetchingFrequency.toMillis))
  val epochInitialWork: Work
  val periodicInitialWork: PeriodicWork


  def start(implicit context: ActorContext) = {
    context.system.scheduler.scheduleOnce(startDelay, context.self, ProduceWork(epochInitialWork))
    //    context.system.scheduler.scheduleOnce(startDelay, context.self, ProduceWork(periodicInitialWork))
  }

  def adjustIngestionWindow(ingestionDates: (DateTime, DateTime)) =
    (ingestionDates._1.plus(fetchingFrequency.toMillis), ingestionDates._2.plus(fetchingFrequency.toMillis))

}

abstract class PeriodicWork(override val source: PeriodicSource,
                            val ingestionDates: (DateTime, DateTime),
                            val isEpoch: Boolean = false) extends Work(source) {

  def generatePeriodicWork(): PeriodicWork


}
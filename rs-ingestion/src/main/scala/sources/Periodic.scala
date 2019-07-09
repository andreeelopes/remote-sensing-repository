package sources

import akka.actor.{ActorContext, Cancellable}
import com.typesafe.config.Config
import org.joda.time.DateTime

import scala.concurrent.duration._

abstract class PeriodicSource(configName: String, config: Config) extends Source(configName, config) {

  val fetchingFrequency: FiniteDuration = config.getDuration(s"sources.$configName.fetching-frequency").getSeconds.seconds
  val epoch: Int = config.getString(s"sources.$configName.epoch").toInt
  val startDelay: FiniteDuration = config.getDuration(s"sources.$configName.start-delay").getSeconds.seconds

  val historyEnd: DateTime = new DateTime()
  val historyDates: (DateTime, DateTime) = (historyEnd.minusYears(epoch), historyEnd)
  val initial: (DateTime, DateTime) = (historyEnd, historyEnd.plus(fetchingFrequency.toMillis))
  val epochWork: Work
  val periodicInitialWork: PeriodicWork

  def generateWork(intervalDates: (DateTime, DateTime), isEpoch: Boolean = false): PeriodicWork

  def start(implicit context: ActorContext): Unit

  def adjustWindow(intervalDates: (DateTime, DateTime)): (DateTime, DateTime) =
    (intervalDates._2, intervalDates._2.plus(fetchingFrequency.toMillis))

}

abstract class PeriodicWork(override val source: PeriodicSource,
                            val intervalDates: (DateTime, DateTime),
                            val isEpoch: Boolean = false) extends Work(source) {

  def generatePeriodicWork(): PeriodicWork


}
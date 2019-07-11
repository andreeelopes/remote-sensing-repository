package sources

import java.util.concurrent.TimeUnit

import akka.actor.{ActorContext, Cancellable}
import com.typesafe.config.Config
import mongo.MongoDAO
import org.joda.time.DateTime
import play.api.libs.json.JsValue

import scala.concurrent.duration._

abstract class PeriodicSource(configName: String, config: Config) extends Source(configName, config) {

  val fetchingFrequency: FiniteDuration =
    Duration((MongoDAO.sourcesJson \ configName \ "fetching-frequency").as[Long], TimeUnit.SECONDS).toSeconds.seconds

  val epoch: Int = (MongoDAO.sourcesJson \ configName \ "epoch").as[Int] // TODO level this to the product and not the source
  val startDelay: FiniteDuration = config.getDuration(s"orchestrator.start-delay").getSeconds.seconds

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
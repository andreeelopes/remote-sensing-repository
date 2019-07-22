package sources

import java.util.concurrent.TimeUnit

import akka.actor.ActorContext
import com.typesafe.config.Config
import mongo.MongoDAO
import org.joda.time.DateTime

import scala.concurrent.duration._

abstract class PeriodicSource(configName: String, config: Config) extends Source(configName, config) {

  val fetchingFrequency: FiniteDuration
  val epoch: Int

  def generateWork(intervalDates: (DateTime, DateTime), isEpoch: Boolean = false): PeriodicWork

  def start(implicit context: ActorContext): Unit
}

abstract class PeriodicWork(override val source: PeriodicSource,
                            val intervalDates: (DateTime, DateTime),
                            val isEpoch: Boolean = false) extends Work(source) {

  def generatePeriodicWork(): PeriodicWork


}
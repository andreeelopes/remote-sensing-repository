package sources

import com.typesafe.config.Config
import org.joda.time.DateTime


abstract class PeriodicRESTSource(configName: String, config: Config) extends PeriodicSource(configName, config) {
  val baseUrl = config.getString(s"sources.$configName.base-url")
  val pageSize = config.getInt(s"sources.$configName.page-size")
}

abstract class PeriodicRESTWork(override val source: PeriodicRESTSource,
                                override val ingestionDates: (DateTime, DateTime),
                                override val isEpoch: Boolean = false,
                                val pageStart: Int = 0) extends PeriodicWork(source, ingestionDates, isEpoch) {

  val url: String

  def preProcess()

  def getGeneratedWork: List[Work]

  def generateNextPagesWork(): Work

}

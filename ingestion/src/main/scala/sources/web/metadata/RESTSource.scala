package sources.web.metadata

import com.typesafe.config.Config
import org.joda.time.DateTime
import sources.web.{HTTPSource, HTTPWork}
import sources.{PeriodicComponent, Work}


abstract class RESTSource(configName: String, config: Config)
  extends HTTPSource(configName, config) with PeriodicComponent

abstract class RESTWork(override val source: RESTSource,
                        val ingestionDates: (DateTime, DateTime),
                        val isEpoch: Boolean = false,
                        val pageStart: Int = 0) extends HTTPWork(source) {

  def preProcess()

  def getNextWork: List[Work]

  def generateNextResourceWork(): Work

  def generatePeriodicWork(): Work
}

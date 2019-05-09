package sources

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import utils.Utils

import scala.concurrent.duration._

case class ExtractionEntry(name: String, queryType: String, resultType: String,
                           path: String, parentExtraction: String, destPath: String)

abstract class Source(configName: String, config: Config) extends Serializable {

  val description = config.getString(s"sources.$configName.description")
  val workTimeout = config.getDuration(s"sources.$configName.work-timeout").getSeconds.seconds
  val retryInterval = config.getDuration(s"sources.$configName.retry-interval").getSeconds.seconds
  val retryTimeout = config.getDuration(s"sources.$configName.retry-timeout").getSeconds.seconds

}


abstract class Work(val source: Source) extends Serializable {

  val workId = Utils.generateUUID()

  def execute()(implicit context: ActorContext, mat: ActorMaterializer)


}





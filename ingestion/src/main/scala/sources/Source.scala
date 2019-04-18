package sources

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import utils.Utils

import scala.concurrent.duration._


abstract class Source(configName: String, config: Config) extends Serializable {

  val description = config.getString(s"sources.$configName.description")
  val workTimeout = config.getDuration(s"sources.$configName.work-timeout").getSeconds.seconds
  val retryInterval = config.getDuration(s"sources.$configName.retry-interval").getSeconds.seconds
  val retryTimeout = config.getDuration(s"sources.$configName.retry-timeout").getSeconds.seconds
  val startDelay = config.getDuration(s"sources.$configName.start-delay").getSeconds.seconds

  val initialWork: Work


}


abstract class Work(val source: Source) extends Serializable {

  val workId = Utils.generateWorkId()

  def execute()(implicit context: ActorContext, mat: ActorMaterializer)

}





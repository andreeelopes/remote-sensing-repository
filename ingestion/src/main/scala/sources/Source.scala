package sources

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import utils.Utils

import scala.concurrent.duration._


abstract class Source(configName: String, config: Config) extends Serializable {

  val description: String = config.getString(s"sources.$configName.description")
  val workTimeout: FiniteDuration = config.getDuration(s"sources.$configName.work-timeout").getSeconds.seconds
  val retryInterval: FiniteDuration = config.getDuration(s"sources.$configName.retry-interval").getSeconds.seconds
  val retryTimeout: FiniteDuration = config.getDuration(s"sources.$configName.retry-timeout").getSeconds.seconds


}


abstract class Work(val source: Source) extends Serializable {

  val workId: String = Utils.generateUUID()

  def execute()(implicit context: ActorContext, mat: ActorMaterializer)

}





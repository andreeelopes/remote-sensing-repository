package sources

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import utils.Utils

import scala.concurrent.duration._
import scala.util.Try


abstract class Source(configName: String, config: Config) extends Serializable {

  val description: String = config.getString(s"sources.$configName.description")
  val workTimeout: FiniteDuration =
    Try(config.getDuration(s"sources.$configName.work-timeout").getSeconds.seconds).getOrElse{
      config.getDuration(s"distributed-workers.work-timeout").getSeconds.seconds
    }

  val baseDir: String = config.getString("clustering.base-dir")



  val authConfigOpt: Option[AuthConfig]
}


abstract class Work(val source: Source) extends Serializable {

  val workId: String = Utils.generateUUID()

  def execute()(implicit context: ActorContext, mat: ActorMaterializer)

  def process(responseBytes: Array[Byte]): List[Work]

}





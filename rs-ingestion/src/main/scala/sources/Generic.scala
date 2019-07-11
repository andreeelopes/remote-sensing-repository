package sources

import java.time
import java.util.concurrent.TimeUnit

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import mongo.MongoDAO
import org.joda.time.DateTime
import sources.handlers.AuthConfig
import utils.Utils

import scala.concurrent.duration._
import scala.util.Try


abstract class Source(val configName: String, val config: Config = ConfigFactory.load()) extends Serializable {

  val baseDir: String = config.getString("clustering.base-dir")

  val authConfigOpt: Option[AuthConfig]
}


abstract class Work(val source: Source) extends Serializable {

  val workId: String = Utils.generateUUID()

  private val sourceWorkTimeoutOpt =
    Try(
      Duration((MongoDAO.sourcesJson \ source.configName \ "work-timeout").as[Long], TimeUnit.SECONDS).toSeconds.seconds
    ).toOption

  var workTimeout: FiniteDuration = sourceWorkTimeoutOpt.getOrElse {
    source.config.getDuration(s"distributed-workers.work-timeout").getSeconds.seconds
  }

  var backOffTimeout = new DateTime()
  var backOffInterval: Double = 0 // seconds

  var nTries = 1

  def execute()(implicit context: ActorContext, mat: ActorMaterializer)

  def process(responseBytes: Array[Byte]): List[Work]

  override def toString: String =
    s"${getClass.getSimpleName}(${workId.substring(0, 4)}, $workTimeout, $backOffInterval s, $backOffTimeout, $nTries)"

}





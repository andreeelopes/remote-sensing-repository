package sources.copernicus

import akka.actor.ActorContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import protocol.worker.WorkExecutor.WorkComplete
import sources.{Source, Work}
import utils.AkkaHTTP
import utils.JsonUtils._
import utils.Utils.getExtractions

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class CreodiasMDSource(config: Config)
  extends Source("creodias.creodias-odata", config) {

  final val configName = "creodias.creodias-odata"

  val baseUrl = config.getString(s"sources.$configName.base-url")
  val extractions = getExtractions(config, configName).filter(e => e.api == "creodias-odata")
}

class CreodiasMDWork(override val source: CreodiasMDSource, productId: String, title: String, platform: String)
  extends Work(source) {
  //TODO platform as enum

  val url = s"${source.baseUrl}/$platform/search.json?maxRecords=1&productIdentifier=%$title%&status=all"

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    implicit val origSender = context.sender

    AkkaHTTP.singleRequest(url).onComplete {
      case Success(response) =>

        Unmarshal(response.entity.withoutSizeLimit).to[String].onComplete {
          case Success(responseString) =>

            process(responseString)

            origSender ! WorkComplete(List())

          case Failure(e) => context.self ! e
            throw new Exception(e)
        }
      case Failure(e) => context.self ! e
        throw new Exception(e)

    }

  }


  private def process(docStr: String) = {
    source.extractions.foreach { e =>
      e.queryType match {
        case "file" => processFile(docStr, e, productId, "", url)
        case "multi-file" => processMultiFile(docStr, e, productId, url)
        case "single-value" => processSingleValue(docStr, e, productId, url)
        case "multi-value" => processMultiValue(docStr, e, productId, url)
      }
    }
  }


}


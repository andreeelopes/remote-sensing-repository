package sources

import akka.actor.ActorContext
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import protocol.worker.WorkExecutor.WorkComplete
import utils.AkkaHTTP
import utils.ParsingUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


case class Extraction(name: String, queryType: String, resultType: String, resultTypeAftrTransf: String,
                      query: String, context: String, destPath: String,
                      contextFormat: String, metamodelMapping: String)


class ExtractionSource(config: Config,
                       configName: String,
                       val extractions: List[Extraction],
                       val authConfig: Option[AuthConfig] = None)
  extends Source(configName, config)

class ExtractionWork(override val source: ExtractionSource, url: String, productId: String, filename: String = "")
  extends Work(source) {


  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    implicit val origSender = context.sender

    AkkaHTTP.singleRequest(url, source.authConfig).onComplete {
      case Success(response) =>
        if (response.status == StatusCode.int2StatusCode(500)) {
          response.discardEntityBytes()
          val e = new Exception("500 - Internal server error of source")
          context.self ! e
          throw e
        }


        Unmarshal(response.entity.withoutSizeLimit).to[Array[Byte]].onComplete {
          case Success(responseBytes) =>

            processExtractions(responseBytes, source.extractions, productId, url, filename)

            origSender ! WorkComplete(List())

          case Failure(e) => context.self ! e
            throw new Exception(e)
        }
      case Failure(e) => context.self ! e
        throw new Exception(e)

    }

  }

}


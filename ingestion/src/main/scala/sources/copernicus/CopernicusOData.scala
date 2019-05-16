package sources

import java.nio.charset.StandardCharsets

import akka.actor.ActorContext
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import org.json.XML
import protocol.worker.WorkExecutor.WorkComplete
import utils.AkkaHTTP
import utils.JsonUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class CopernicusODataSource(configName: String, config: Config, val extractions: List[ExtractionEntry])
  extends Source(configName, config) with AuthComponent {

  override val authConfigOpt = Some(AuthConfig(configName, config))
}

class CopernicusODataWork(override val source: CopernicusODataSource, val url: String, val productId: String, filename: String)
  extends Work(source) { // TODO variable timeout depending on the size of the object


  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {

    implicit val origSender = context.sender

    AkkaHTTP.singleRequest(url, source.authConfigOpt).onComplete {
      case Success(response) =>

        if (response.status == StatusCode.int2StatusCode(500)) {
          response.discardEntityBytes()
          val e = new Exception("500 - Internal server error of source")
          context.self ! e
          throw e
        }

        Unmarshal(response.entity.withoutSizeLimit).to[Array[Byte]].onComplete {
          case Success(responseBytes) =>

            process(responseBytes)

            origSender ! WorkComplete(List())

          case Failure(e) => context.self ! e
            throw new Exception(e)
        }

      case Failure(e) => context.self ! e
        throw new Exception(e)

    }

  }


  private def process(responseBytes: Array[Byte]) = {
    source.extractions.foreach { e =>
      e.queryType match {
        case "file" =>
          val xml = new String(responseBytes, StandardCharsets.UTF_8)

          processFile(XML.toJSONObject(xml).toString(3), e, productId, filename, url)
        case "multi-file" =>
          val xml = new String(responseBytes, StandardCharsets.UTF_8)

          processMultiFile(XML.toJSONObject(xml).toString(3), e, productId, url)
        case "single-value" =>
          val xml = new String(responseBytes, StandardCharsets.UTF_8)

          processSingleValue(XML.toJSONObject(xml).toString(3), e, productId, url)
        case "multi-value" =>
          val xml = new String(responseBytes, StandardCharsets.UTF_8)

          processMultiValue(XML.toJSONObject(xml).toString(3), e, productId, url)
      }

    }

  }

  //  private def process(responseBytes: Array[Byte]) = {
  //    source.extractions.foreach { e =>
  //      e.queryType match {
  //        case "file" if e.resultType == "xml" =>
  //          if (e.resultType == "xml")
  //            processFile(new String(responseBytes, StandardCharsets.UTF_8), e, productId, filename, url)
  //        case "multi-file" =>
  //          processMultiFile(new String(responseBytes, StandardCharsets.UTF_8), e, productId, url)
  //        case "single-value" =>
  //          processSingleValue(new String(responseBytes, StandardCharsets.UTF_8), e, productId, url)
  //        case "multi-value" =>
  //          processMultiValue(new String(responseBytes, StandardCharsets.UTF_8), e, productId, url)
  //        case _ =>
  //          Utils.processFile(responseBytes, e, productId, filename, url) //big files problem
  //
  //      }
  //    }
  //
  //  }


}



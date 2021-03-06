package sources

import akka.actor.{ActorContext, ActorRef}
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import sources.handlers.{AuthConfig, ErrorHandlers}
import utils.HTTPClient._
import sources.handlers.Parsing._


case class Extraction(name: String, queryType: String, resultType: String, resultTypeAftrTransf: String,
                      query: String, context: String, destPath: String,
                      contextFormat: String, metamodelMapping: String, collection: String, dtfStr: String, updateUrl: Boolean)


class ExtractionSource(config: Config,
                       configName: String,
                       val extractions: List[Extraction],
                       val errorHandler: (Int, Array[Byte], String, ActorMaterializer) => Unit = ErrorHandlers.defaultErrorHandler,
                       val processOpt: Option[Array[Byte] => List[Work]] = None,
                       val authConfig: Option[AuthConfig] = None)
  extends Source(configName, config) {
  override val authConfigOpt: Option[AuthConfig] = authConfig
}

class ExtractionWork(override val source: ExtractionSource, url: String, productId: String, filename: String = "")
  extends Work(source) {

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    source.processOpt match {
      case Some(processFunc) => singleRequest(url, workTimeout, processFunc, source.errorHandler, source.authConfigOpt)
      case None => singleRequest(url, workTimeout, process, source.errorHandler, source.authConfigOpt)
    }
  }

  override def process(responseBytes: Array[Byte]): List[Work] = {
    processExtractions(responseBytes, source.extractions, productId, url, filename)

    List()
  }

}



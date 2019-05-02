package sources

import java.io.File

import akka.actor.ActorContext
import akka.http.scaladsl.model.StatusCode
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import com.typesafe.config.Config
import protocol.worker.WorkExecutor.WorkComplete
import utils.AkkaHTTP

import scala.concurrent.ExecutionContext.Implicits.global


class FetchAndSaveSource(configName: String, config: Config) extends Source(configName, config) with AuthComponent {
  override val authConfigOpt = Some(AuthConfig(configName, config))
}

class FetchAndSaveWork(override val source: FetchAndSaveSource, val url: String, val destPath: String)
  extends Work(source) { // TODO variable timeout depending on the size of the object

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {

    implicit val origSender = context.sender
    val responseFuture = AkkaHTTP.singleRequest(url, source.authConfigOpt)

    val streamSource = Source.fromFuture(responseFuture).flatMapConcat { response =>
      if (response.status == StatusCode.int2StatusCode(500)) {
        response.discardEntityBytes()
        val e = new Exception("500 - Internal server error of source")
        context.self ! e
        throw e
      }

      response.entity.withoutSizeLimit.dataBytes

    }


    val streamSink = FileIO.toPath(new File(destPath).toPath)

    streamSource.runWith(streamSink).onComplete { _ => origSender ! WorkComplete(List()) }


  }

}



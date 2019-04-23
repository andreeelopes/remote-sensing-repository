package sources

import java.io.File

import akka.actor.ActorContext
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import com.typesafe.config.Config
import protocol.worker.WorkExecutor.WorkComplete
import utils.AkkaHTTP

class FetchAndSaveSource(configName: String, config: Config) extends Source(configName, config) with AuthComponent {
  override val authConfigOpt = Some(AuthConfig(configName, config))
}

class FetchAndSaveWork(override val source: FetchAndSaveSource, val url: String, val destPath: String)
  extends Work(source) { //TODO streaming

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer) = {

    implicit val origSender = context.sender

    println("FETCHING " + url)
    println("SAVING TO " + destPath)

    val responseFuture = AkkaHTTP.singleRequest(url, source.authConfigOpt)

    Source.fromFuture(responseFuture).flatMapConcat {
      case HttpResponse(_, _, entity, _) => entity.withoutSizeLimit.dataBytes
    }.runWith(FileIO.toPath(new File(destPath).toPath))

    origSender ! WorkComplete(List())
  }

}



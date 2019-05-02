package sources.copernicus

import java.io.File

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import com.typesafe.config.Config
import protocol.worker.WorkExecutor.WorkComplete
import sources.{AuthComponent, AuthConfig, Work}
import utils.AkkaHTTP

import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.Elem

//Todo ter em conta tamanho do download - stream vs single request
class CopernicusODataMDSource(config: Config)
  extends sources.Source("copernicusOAHOData", config) with AuthComponent {
  override val authConfigOpt = Some(AuthConfig("copernicusOAHOData", config))
  val baseUrl = config.getString(s"sources.copernicusOAHOData.base-url")
  // TODO timeout based on the size of object
}


class CopernicusODataMDWork(override val source: CopernicusODataMDSource, val productId: String)
  extends Work(source) {

  val url = s"${source.baseUrl}Products('$productId')/"

  var resource: Elem = _

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer) = {

    implicit val origSender = context.sender

    val responseFuture = AkkaHTTP.singleRequest(url, source.authConfigOpt)


    Source.fromFuture(responseFuture).flatMapConcat { response =>

      //      Unmarshal(response.entity).to[String].onComplete {
      //        case Success(responseString) =>
      //          resource = XML.loadString(responseString)
      //
      //          val online = (resource \ "properties" \ "Online").text.toBoolean
      //
      //        case Failure(e) => context.self ! e
      //          throw new Exception(e)
      //      }

      response.entity.dataBytes

    }.runWith(FileIO.toPath(new File(s"$productId/odataMD.xml").toPath))
      .onComplete { _ => origSender ! WorkComplete(List()) }

  }

}




package sources

import java.io.File

import akka.actor.ActorContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import com.typesafe.config.Config
import protocol.worker.WorkExecutor.WorkComplete
import utils.AkkaHTTP

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.xml.{Elem, XML}

//Todo ter em conta tamanho do download - stream vs single request
class CopernicusODataMDSource(config: Config)
  extends Source("copernicusOAHOData", config) with AuthComponent {
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

    AkkaHTTP.singleRequest(url, source.authConfigOpt).onComplete {
      case Success(response) =>

        val path = s"$productId\\odataMD.xml"
        response.entity.dataBytes.runWith(FileIO.toPath(new File(path).toPath))

        Unmarshal(response.entity).to[String].onComplete {

          case Success(responseString) =>
            resource = XML.loadString(responseString)

            val online = (resource \ "properties" \ "Online").text.toBoolean

            origSender ! WorkComplete(List())

          case Failure(e) => throw new Exception(e)
        }
      case Failure(e) => throw new Exception(e)
    }


  }

}




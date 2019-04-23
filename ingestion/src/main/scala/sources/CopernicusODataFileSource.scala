package sources

import java.io.File

import akka.actor.ActorContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import protocol.worker.WorkExecutor.WorkComplete
import utils.AkkaHTTP

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.xml.{Elem, XML}

//Todo ter em conta tamanho do download - stream vs single request
class CopernicusODataFileSource(val config: Config)
  extends Source("copernicusOAHOData", config) with AuthComponent {
  override val authConfigOpt = Some(AuthConfig("copernicusOAHOData", config)) //TODO não pode estar sempre a ir ao disco
  // TODO timeout based on the size of object
}


class CopernicusODataFileWork(override val source: CopernicusODataFileSource, val url: String, val productId: String)
  extends Work(source) {

  var resource: Elem = _

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer) = {


    implicit val origSender = context.sender

    AkkaHTTP.singleRequest(url, source.authConfigOpt).onComplete {
      case Success(response) =>

        Unmarshal(response.entity).to[String].onComplete {
          case Success(responseString) =>
            resource = XML.loadString(responseString)

            var workToBeDone = List[Work]()

            (resource \ "entry").foreach { node =>
              //TODO ignore IMG_DATA

              //              if ((node \ "title").text != "IMG_DATA") {

              val resourceUrl = (node \ "id").text
              val length = (node \ "properties" \ "ContentLength").text.toInt

              val nodesQuery = resourceUrl.split("/Nodes").map {
                //('L2A_T13XDG_A011094_20190421T205226') => L2A_T13XDG_A011094_20190421T205226
                node => node.substring(2, node.length - 2)
              }.drop(1) // remove base url

              val path = s"$productId/${nodesQuery.mkString("/")}"


              if (length == 0) {
                // TODO check if it has more than 2 concurrent requests
                new File(path).mkdirs()
                workToBeDone ::= new CopernicusODataFileWork(source, s"$resourceUrl/Nodes", productId)
              } else
                workToBeDone ::= new FetchAndSaveWork(
                  new FetchAndSaveSource("copernicusOAHOData", source.config), //TODO add own conf
                  s"$resourceUrl/$$value", path)

              //              }

            }








            //get next
            //save this
            //avoid img_data
            //work-failed tem de ser retentado

            origSender ! WorkComplete(workToBeDone)

          case Failure(e) => throw new Exception(e)
        }
      case Failure(e) => throw new Exception(e)
    }


  }

}




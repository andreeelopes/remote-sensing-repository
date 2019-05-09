package sources.copernicus

import akka.actor.ActorContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import protocol.worker.WorkExecutor.WorkComplete
import sources._
import utils.AkkaHTTP
import utils.XmlUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.xml.{Elem, NodeSeq, XML}

class CopernicusManifestSource(val config: Config)
  extends sources.Source("copernicus.copernicus-oah-odata", config) with AuthComponent {

  final val configName = "copernicus.copernicus-oah-odata"

  override val authConfigOpt = Some(AuthConfig(configName, config))
  val baseUrl = config.getString(s"sources.$configName.base-url")

  val extractions = getExtractions(config, configName)

}


class CopernicusManifestWork(override val source: CopernicusManifestSource, val productId: String, val title: String)
  extends Work(source) {

  val url = s"${source.baseUrl}Products('$productId')/Nodes('$title.SAFE')/Nodes('manifest.safe')/$$value"

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer) = {

    implicit val origSender = context.sender


    AkkaHTTP.singleRequest(url, source.authConfigOpt).onComplete {
      case Success(response) =>

        Unmarshal(response.entity).to[String].onComplete {
          case Success(responseString) =>

            val workToBeDone = process(XML.loadString(responseString))

            origSender ! WorkComplete(workToBeDone)

          case Failure(e) => context.self ! e
            throw new Exception(e)
        }

      case Failure(e) => context.self ! e
        throw new Exception(e)

    }

  }

  private def process(doc: Elem) = {
    var workToBeDone = List[Work]()

    val dataObjects = doc \ "dataObjectSection" \ "dataObject"

    XML.save(s"data/$productId/manifest.safe", doc)

    var extMap = Map[String, List[ExtractionEntry]]()
    source.extractions.foreach { e =>
      val id = if (e.parentExtraction == "") e.name else e.parentExtraction
      val set = e :: extMap.getOrElse(id, List())

      extMap += (id -> set)
    }

    extMap.foreach { case (k, v) => workToBeDone ::= processFileExtraction(dataObjects, k, v, workToBeDone) }

    workToBeDone
  }

  private def processFileExtraction(dataObjects: NodeSeq,
                                    id: String,
                                    extractions: List[ExtractionEntry],
                                    workToBeDone: List[Work]) = {


    val node = dataObjects.filter(node => (node \@ "ID") == id).head
    val path = node \ "byteStream" \ "fileLocation" \@ "href"

    //  e.g.  path = ./GRANULE/L1C_T29SND_A009687_20190113T113432/IMG_DATA/T29SND_20190113T113429_B01.jp2
    val pathFragments = path.split("/").drop(1) // [GRANULE, L1C_T29SND_A009687_20190113T113432,...]
    val filePath = pathFragments.map(p => s"Nodes('$p')").mkString("/") + "/$value" // Nodes('GRANULE')/.../$value

    val fileUrl = s"${
      source.baseUrl
    }Products('$productId')/Nodes('$title.SAFE')/" + filePath

    new CopernicusODataWork(new CopernicusODataSource(source.configName, source.config, extractions),
      fileUrl, productId, pathFragments.last)
  }


}




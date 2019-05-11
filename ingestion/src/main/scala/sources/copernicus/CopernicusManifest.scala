package sources.copernicus

import akka.actor.ActorContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import protocol.worker.WorkExecutor.WorkComplete
import sources._
import utils.AkkaHTTP
import utils.Utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.xml.{Elem, NodeSeq, XML}

class CopernicusManifestSource(val config: Config)
  extends sources.Source("copernicus.copernicus-oah-odata", config) with AuthComponent {

  final val configName = "copernicus.copernicus-oah-odata"

  override val authConfigOpt = Some(AuthConfig(configName, config))
  val baseUrl = config.getString(s"sources.$configName.base-url")

  val extractions = getExtractions(config, configName).filter(e => e.api == "copernicus-odata")

}

//TODO para o L2 também vem o manifest do L1

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
    var extractions = List[ExtractionEntry]()

    XML.save(s"data/$productId/manifest.safe", doc)

    val containerExtractions = source.extractions.filter(e => e.queryType == "container")

    containerExtractions.foreach(e => extractions :::= processContainerExtraction(e, doc))
    extractions :::= source.extractions.filterNot(e => e.queryType == "container")

    var extMap = Map[String, List[ExtractionEntry]]()
    extractions.foreach { e =>
      val id = if (e.parentExtraction == "") e.name else e.parentExtraction
      val set = e :: extMap.getOrElse(id, List())

      extMap += (id -> set)
    }

    extMap.foreach { case (k, v) => workToBeDone ::= processFileExtraction(doc, k, v, workToBeDone) }

    workToBeDone
  }

  private def processFileExtraction(doc: NodeSeq,
                                    id: String,
                                    extractions: List[ExtractionEntry],
                                    workToBeDone: List[Work]) = {

    val dataObjects = doc \ "dataObjectSection" \ "dataObject"

    val node = dataObjects.filter(node => (node \@ "ID") == id).head
    val path = node \ "byteStream" \ "fileLocation" \@ "href"

    //  e.g.  path = ./GRANULE/L1C_T29SND_A009687_20190113T113432/IMG_DATA/T29SND_20190113T113429_B01.jp2
    val pathFragments = path.split("/").drop(1) // [GRANULE, L1C_T29SND_A009687_20190113T113432,...]
    val filePath = pathFragments.map(p => s"Nodes('$p')").mkString("/") + "/$value" // Nodes('GRANULE')/.../$value

    val fileUrl = s"${source.baseUrl}Products('$productId')/Nodes('$title.SAFE')/" + filePath

    new CopernicusODataWork(new CopernicusODataSource(source.configName, source.config, extractions),
      fileUrl, productId, pathFragments.last)
  }

  private def processContainerExtraction(extractionEntry: ExtractionEntry, doc: Elem) = {
    val container =
      (doc \ "informationPackageMap" \ "contentUnit" \\ "contentUnit")
        .filter(n => (n \@ "ID") == extractionEntry.name).head

    (container \\ "dataObjectPointer")
      .map(n => n \@ "dataObjectID")
      .map { id =>
        ExtractionEntry(id, "file", "undefined", "/", "", "./data/(productId)/(filename)", "copernicus-odata")
      }.toList

  }


}




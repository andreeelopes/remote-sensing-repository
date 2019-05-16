package sources.copernicus

import akka.actor.ActorContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import org.json.XML
import play.api.libs.json.{JsValue, Json}
import protocol.worker.WorkExecutor.WorkComplete
import sources._
import utils.AkkaHTTP
import utils.Utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class CopernicusManifestSource(val config: Config, platform: String, productType: String)
  extends sources.Source("copernicus.copernicus-oah-odata", config) with AuthComponent {

  final val configName = "copernicus.copernicus-oah-odata"

  override val authConfigOpt = Some(AuthConfig(configName, config))
  val baseUrl = config.getString(s"sources.$configName.base-url")

  //  mapping of manifest names

  val sentinel2Ext = getExtractions(config, s"$configName.${platform.toLowerCase}")
    .filter(e => e.api == "copernicus-odata")

  val productTypeSpecificExt = getExtractions(config, s"$configName.$productType")
    .filter(e => e.api == "copernicus-odata")

  val extractions = sentinel2Ext ::: productTypeSpecificExt

  val manifestName = "manifest.safe"
  //    if (productType == "S2MSI2A")
  //      "L2A_Manifest"
  //    else if (platform == "Sentinel3")
  //      "xfdumanifest.xml"
  //    else
  //      "manifest.safe"
}

class CopernicusManifestWork(override val source: CopernicusManifestSource, val productId: String, val title: String)
  extends Work(source) {

  val url = s"${source.baseUrl}Products('$productId')/Nodes('$title.SAFE')/Nodes('${source.manifestName}')/$$value"

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer) = {

    implicit val origSender = context.sender


    AkkaHTTP.singleRequest(url, source.authConfigOpt).onComplete {
      case Success(response) =>

        Unmarshal(response.entity.withoutSizeLimit).to[String].onComplete {
          case Success(responseString) =>

            val workToBeDone = process(XML.toJSONObject(responseString).toString(3))

            origSender ! WorkComplete(workToBeDone)

          case Failure(e) => context.self ! e
            throw new Exception(e)
        }

      case Failure(e) => context.self ! e
        throw new Exception(e)

    }

  }

  private def process(docJson: String) = {
    val doc = Json.parse(docJson)
    var workToBeDone = List[Work]()
    var extractions = List[ExtractionEntry]()

    writeFile(s"data/$productId/${source.manifestName}", docJson)

    val containerExtractions = source.extractions.filter(e => e.queryType == "container")

    //    containerExtractions.foreach(e => extractions :::= processContainerExtraction(e, doc))
    extractions :::= source.extractions.filterNot(e => e.queryType == "container")

    var extMap = Map[String, List[ExtractionEntry]]()
    extractions.foreach { e =>
      val id = if (e.docContext == "") e.name else e.docContext
      val set = e :: extMap.getOrElse(id, List())

      extMap += (id -> set)
    }

    extMap.foreach { case (k, v) => workToBeDone ::= processFileExtraction(doc, k, v, workToBeDone) }

    workToBeDone
  }

  private def processFileExtraction(doc: JsValue,
                                    id: String,
                                    extractions: List[ExtractionEntry],
                                    workToBeDone: List[Work]) = {

    val dataObjects = (doc \ "xfdu:XFDU" \ "dataObjectSection" \ "dataObject").as[List[JsValue]]
    val node = dataObjects.filter(node => (node \ "ID").as[String] == id).head
    val path = (node \ "byteStream" \ "fileLocation" \ "href").as[String]

    //  e.g.  path = ./GRANULE/L1C_T29SND_A009687_20190113T113432/IMG_DATA/T29SND_20190113T113429_B01.jp2
    val pathFragments = path.split("/").drop(1) // [GRANULE, L1C_T29SND_A009687_20190113T113432,...]
    val filePath = pathFragments.map(p => s"Nodes('$p')").mkString("/") + "/$value" // Nodes('GRANULE')/.../$value

    val fileUrl = s"${source.baseUrl}Products('$productId')/Nodes('$title.SAFE')/" + filePath

    new CopernicusODataWork(new CopernicusODataSource(source.configName, source.config, extractions),
      fileUrl, productId, pathFragments.last)
  }

  //  private def processContainerExtraction(extractionEntry: ExtractionEntry, doc: JsValue) = {
  //    val container = (doc \ "informationPackageMap" \ "contentUnit" \\ "contentUnit")
  //        .filter(n => (n \ "ID").as[String] == extractionEntry.name).head
  //
  //    (container \\ "dataObjectPointer")
  //      .map(n => n \@ "dataObjectID")
  //      .map { id =>
  //        ExtractionEntry(id, "file", "undefined", "/", "", "./data/(productId)/(filename)", "copernicus-odata")
  //      }.toList
  //
  //  }


}




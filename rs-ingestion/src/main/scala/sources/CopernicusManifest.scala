package sources

import java.nio.charset.StandardCharsets

import akka.actor.{ActorContext, ActorRef}
import akka.stream.ActorMaterializer
import com.jayway.jsonpath.JsonPath
import com.typesafe.config.Config
import mongo.MongoDAO
import net.minidev.json.JSONArray
import org.json.XML
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonInt64, BsonString}
import play.api.libs.json.{JsValue, Json}
import sources.handlers.{AuthComponent, AuthConfig, ErrorHandlers}
import utils.{HTTPClient, ResourceDoesNotExistException}
import utils.HTTPClient._
import sources.handlers.Parsing.processExtractions
import utils.Utils._
import sources.handlers.ManifestUtils._

import scala.util.{Failure, Success, Try}


class CopernicusManifestSource(config: Config, val program: String, val platform: String, val productType: String)
  extends sources.Source("copernicus-oah-odata", config) with AuthComponent {

  override val configName = "copernicus-oah-odata"

  override val authConfigOpt = Some(AuthConfig(configName, config))

  val baseUrl: String = config.getString(s"sources.$configName.base-url")

  val extractions: List[Extraction] = getAllExtractions(configName, program, platform.toLowerCase, productType)

  //  mapping of manifest names
  val manifestName: String = if (platform == "sentinel3") "xfdumanifest.xml" else "manifest.safe"
  val manifestFormat: String = if (platform == "sentinel3") "SEN3" else "SAFE"

}

class CopernicusManifestWork(override val source: CopernicusManifestSource, val productId: String, val title: String)
  extends Work(source) {


  val url = s"${source.baseUrl}Products('$productId')/Nodes('$title.${source.manifestFormat}')/Nodes('${source.manifestName}')/$$value"

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    singleRequest(url, workTimeout, process, ErrorHandlers.copernicusODataErrorHandler, source.authConfigOpt)
  }

  override def process(responseBytes: Array[Byte]): List[Work] = {
    var workToBeDone = List[Work]()
    var extractions = List[Extraction]()

    try {
      val docStr = XML.toJSONObject(new String(responseBytes, StandardCharsets.UTF_8)).toString
      val dataObjects = Try {
        (Json.parse(docStr) \ "xfdu:XFDU" \ "dataObjectSection" \ "dataObject").as[List[JsValue]]
      } match {
        case Failure(_) => throw ResourceDoesNotExistException()
        case Success(value) => value
      }

      // process manifest extractions
      val manifestExtractions = source.extractions
        .filter(e => e.name == "manifest" || e.context == "manifest")
      processExtractions(responseBytes, manifestExtractions, productId, url).right.get

      // generate data URLs
      processObjectsURL(dataObjects)

      // split container extractions into file extractions
      val containerExtractions = source.extractions.filter(e => e.queryType == "container")
      containerExtractions.foreach(e => extractions :::= processContainerExtraction(e, docStr))
      extractions :::= source.extractions.diff(containerExtractions).diff(manifestExtractions)

      // aggregate queries
      var extMap = Map[String, List[Extraction]]()
      extractions.foreach { e =>
        val id = if (e.context == "") e.name else e.context
        val set = e :: extMap.getOrElse(id, List())

        extMap += (id -> set)
      }

      extMap.foreach { case (k, v) => workToBeDone ::= processFileExtraction(dataObjects, k, v, workToBeDone) }

    } catch {
      case e: Exception => e.printStackTrace()
    }

    workToBeDone
  }


  private def processObjectsURL(dataObjects: List[JsValue]): Unit = {

    val mongoSubDoc =
      if (source.productType == "S2MSI1C") processObjectsS2MSI1C(dataObjects, transformURL)
      else if (source.productType == "S2MSI2A") processObjectsS2MSI2A(dataObjects, transformURL)
      else if (source.platform == "sentinel1") processObjectsSentinel1(dataObjects, transformURL)
      else if (source.productType == "OL_1_ERR___")
        processObjectsOL_1_ERR___(dataObjects, transformURL)
      else
        processObjectsGeneric(dataObjects, transformURL)

    MongoDAO.addFieldToDoc(productId, "data", mongoSubDoc, MongoDAO.PRODUCTS_COL)
  }


  private def processFileExtraction(dataObjects: List[JsValue],
                                    id: String,
                                    extractions: List[Extraction],
                                    workToBeDone: List[Work]) = {

    val node = dataObjects.filter(node => (node \ "ID").as[String] == id).head
    val path = (node \ "byteStream" \ "fileLocation" \ "href").as[String]

    val fileUrl = transformURL(path)

    new ExtractionWork(
      new ExtractionSource(source.config, source.configName, extractions, ErrorHandlers.defaultErrorHandler, None, source.authConfigOpt),
      fileUrl._1, productId, fileUrl._2)
  }

  def transformURL(path: String): (String, String) = {
    //  e.g.  path = ./GRANULE/L1C_T29SND_A009687_20190113T113432/IMG_DATA/T29SND_20190113T113429_B01.jp2
    val pathFragments = path.split("/").drop(1) // [GRANULE, L1C_T29SND_A009687_20190113T113432,...]
    val filePath = pathFragments.map(p => s"Nodes('$p')").mkString("/") + "/$value" // Nodes('GRANULE')/.../$value

    (s"${source.baseUrl}Products('$productId')/Nodes('$title.${source.manifestFormat}')/" + filePath, pathFragments.last)
  }

  private def processContainerExtraction(extraction: Extraction, doc: String) = {
    val result = JsonPath.read[JSONArray](doc,
      s"$$.xfdu:XFDU.informationPackageMap.xfdu:contentUnit..xfdu:contentUnit[?(@.ID=='${extraction.name}')]..dataObjectID").toJSONString

    Json.parse(result)
      .as[List[String]]
      .map(id => Extraction(id, "file", "undefined", "", "$", "", "./data/(productId)/(filename)", "", extraction.metamodelMapping, "", null, false))

  }


}




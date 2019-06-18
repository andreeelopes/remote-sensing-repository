package sources

import akka.actor.{ActorContext, ActorRef}
import akka.stream.ActorMaterializer
import com.jayway.jsonpath.JsonPath
import com.typesafe.config.Config
import mongo.MongoDAO
import net.minidev.json.JSONArray
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}
import play.api.libs.json.{JsValue, Json}
import utils.HTTPClient
import utils.HTTPClient._
import utils.Parsing.processExtractions
import utils.Utils._

object CopernicusManifest {
  final val configName = "copernicus.copernicus-oah-odata"
  final val manifestExt = "manifest"
}

class CopernicusManifestSource(val config: Config, program: String, platform: String, productType: String)
  extends sources.Source(CopernicusManifest.configName, config) with AuthComponent {

  final val configName = CopernicusManifest.configName

  override val authConfigOpt = Some(AuthConfig(configName, config))

  val baseUrl: String = config.getString(s"sources.$configName.base-url")

  val extractions: List[Extraction] = getAllExtractions(config, configName, program, platform.toLowerCase, productType)

  //  mapping of manifest names
  val manifestName: String = if (platform == "sentinel3") "xfdumanifest.xml" else "manifest.safe"
  val manifestFormat: String = if (platform == "sentinel3") "SEN3" else "SAFE"

}

class CopernicusManifestWork(override val source: CopernicusManifestSource, val productId: String, val title: String)
  extends Work(source) {

  val url = s"${source.baseUrl}Products('$productId')/Nodes('$title.${source.manifestFormat}')/Nodes('${source.manifestName}')/$$value"

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    singleRequest(url, source.workTimeout, process, ErrorHandlers.copernicusODataErrorHandler, source.authConfigOpt)
  }

  override def process(responseBytes: Array[Byte]): List[Work] = {
    var workToBeDone = List[Work]()
    var extractions = List[Extraction]()

    try {

      // process manifest extractions
      val manifestExtractions = source.extractions
        .filter(e => e.name == CopernicusManifest.manifestExt || e.context == CopernicusManifest.manifestExt)
      val doc = processExtractions(responseBytes, manifestExtractions, productId, url).right.get

      val dataObjects = (Json.parse(doc) \ "xfdu:XFDU" \ "dataObjectSection" \ "dataObject").as[List[JsValue]]

      processObjectsURL(dataObjects)

      // split container extractions into file extractions
      val containerExtractions = source.extractions.filter(e => e.queryType == "container")
      containerExtractions.foreach(e => extractions :::= processContainerExtraction(e, doc))
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
      case e: Exception => e.printStackTrace() //resource doesnt exist
    }

    workToBeDone
  }

  private def processObjectsURL(dataObjects: List[JsValue]): Unit = {

    var rootDoc = BsonDocument()

    dataObjects.foreach { obj =>
      val manifestId = (obj \ "ID").as[String]
      val href = (obj \ "byteStream" \ "fileLocation" \ "href").as[String]

      val fileUrl = transformURL(href)._1

      rootDoc = rootDoc.append(manifestId, BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(fileUrl)))
    }

    MongoDAO.addFieldToDoc(productId, "data", rootDoc, MongoDAO.PRODUCTS_COL)
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




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
      if (source.platform == "sentinel2") processObjectsSentinel2(dataObjects)
      else if (source.platform == "sentinel1") processObjectsSentinel1(dataObjects)
      // sentinel 3 products are hard to unify
      else if (source.productType == "OL_1_ERR___")
        processObjectsOL_1_ERR___(dataObjects)
      else
        processObjectsGeneric(dataObjects)

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


  private def processObjectsGeneric(dataObjects: List[JsValue]): BsonDocument = {
    var mongoSubDoc = BsonDocument()

    dataObjects.foreach { obj =>
      val manifestId = (obj \ "ID").as[String]
      val href = (obj \ "byteStream" \ "fileLocation" \ "href").as[String]

      val fileUrl = transformURL(href)._1

      mongoSubDoc.append(manifestId, BsonDocument("status" -> BsonString("remote"), "url" -> BsonString(fileUrl)))
    }
    mongoSubDoc
  }

  private def processObjectsSentinel2(dataObjects: List[JsValue]): BsonDocument = {
    var mongoSubDoc = BsonDocument()
    var imageryDoc = BsonArray()

    dataObjects.foreach { obj =>
      val manifestId = (obj \ "ID").as[String]
      val href = (obj \ "byteStream" \ "fileLocation" \ "href").as[String]
      val size = (obj \ "byteStream" \ "size").as[Long]
      val fileUrl = transformURL(href)._1

      if (manifestId.startsWith("IMG_DATA")) {
        //  IMG_DATA_Band_10m_1_Tile1_Data -> [IMG, DATA, Band, 10m, 1, Tile1, Data]
        val splitId = manifestId.split("_")

        imageryDoc.add(
          BsonDocument(
            "band" -> BsonString(splitId(4)),
            "tile" -> BsonString(splitId(5)),
            "resolution" -> BsonString(splitId(3)),
            "url" -> BsonString(fileUrl),
            "status" -> BsonString("remote"),
            "size" -> BsonInt64(size)
          ))
      }
      else {
        val updatedMongoSubDoc = BsonDocument(
          "status" -> BsonString("remote"),
          "url" -> BsonString(fileUrl),
          "size" -> BsonInt64(size)
        )
        mongoSubDoc.append(manifestId, updatedMongoSubDoc)
      }
    }

    mongoSubDoc.append("imagery", imageryDoc)
    mongoSubDoc
  }

  private def processObjectsSentinel1(dataObjects: List[JsValue]): BsonDocument = {
    var mongoSubDoc = BsonDocument()
    var imageryDoc = BsonArray()

    dataObjects.foreach { obj =>
      val manifestId = (obj \ "ID").as[String]
      val href = (obj \ "byteStream" \ "fileLocation" \ "href").as[String]
      val size = (obj \ "byteStream" \ "size").as[Long]
      val fileUrl = transformURL(href)._1

      //  ./measurement/s1a-ew1-slc-hh-20190626t185351-20190626t185422-027854-03250b-001.tiff
      if (href.endsWith(".tiff")) {
        // s1a-ew1-slc-hh-20190626t185351-20190626t185422-027854-03250b-001

        val name = href.split("/")(2).replaceAll(".tiff", "").split("-")

        imageryDoc.add(
          BsonDocument(
            "polarisation" -> BsonString(name(3)),
            "swathId" -> BsonString(name(1)),
            "imageNumber" -> BsonString(name.last),
            "url" -> BsonString(fileUrl),
            "status" -> BsonString("remote"),
            "size" -> BsonInt64(size)
          ))
      }
      else {
        val updatedMongoSubDoc = BsonDocument(
          "status" -> BsonString("remote"),
          "url" -> BsonString(fileUrl),
          "size" -> BsonInt64(size)
        )
        mongoSubDoc.append(manifestId, updatedMongoSubDoc)
      }
    }

    mongoSubDoc.append("imagery", imageryDoc)
    mongoSubDoc
  }

  private def processObjectsOL_1_ERR___(dataObjects: List[JsValue]): BsonDocument = {
    var mongoSubDoc = BsonDocument()
    var imageryDoc = BsonArray()

    dataObjects.foreach { obj =>
      val manifestId = (obj \ "ID").as[String]
      val href = (obj \ "byteStream" \ "fileLocation" \ "href").as[String]
      val size = (obj \ "byteStream" \ "size").as[Long]
      val fileUrl = transformURL(href)._1

      if (manifestId.endsWith("radianceData")) {
        //  Oa01_radianceUnit -> [Oa01, radianceUnit]
        val splitId = manifestId.split("_")

        imageryDoc.add(
          BsonDocument(
            "band" -> BsonString(splitId(0)),
            "url" -> BsonString(fileUrl),
            "status" -> BsonString("remote"),
            "size" -> BsonInt64(size)
          ))
      }
      else {
        val updatedMongoSubDoc = BsonDocument(
          "status" -> BsonString("remote"),
          "url" -> BsonString(fileUrl),
          "size" -> BsonInt64(size)
        )
        mongoSubDoc.append(manifestId, updatedMongoSubDoc)
      }
    }

    mongoSubDoc.append("imagery", imageryDoc)
    mongoSubDoc
  }


  private def processContainerExtraction(extraction: Extraction, doc: String) = {
    val result = JsonPath.read[JSONArray](doc,
      s"$$.xfdu:XFDU.informationPackageMap.xfdu:contentUnit..xfdu:contentUnit[?(@.ID=='${extraction.name}')]..dataObjectID").toJSONString

    Json.parse(result)
      .as[List[String]]
      .map(id => Extraction(id, "file", "undefined", "", "$", "", "./data/(productId)/(filename)", "", extraction.metamodelMapping, "", null, false))

  }


}




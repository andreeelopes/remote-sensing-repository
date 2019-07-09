package sources.handlers

import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonInt64, BsonString}
import play.api.libs.json.JsValue
import utils.Utils.generateUUID

object ManifestUtils {

  def processObjectsGeneric(dataObjects: List[JsValue], transformURL: String => (String, String)): BsonDocument = {
    var mongoSubDoc = BsonDocument()

    dataObjects.foreach { obj =>
      val manifestId = (obj \ "ID").as[String]
      val href = (obj \ "byteStream" \ "fileLocation" \ "href").as[String]
      val fileName = href.split("/").last
      val size = (obj \ "byteStream" \ "size").as[Long]

      val fileUrl = transformURL(href)._1

      val mongoSubDoc =
          BsonDocument(
            "_id" -> BsonString(generateUUID()),
            "status" -> BsonString("remote"),
            "fileName" -> BsonString(fileName),
            "url" -> BsonString(fileUrl),
            "size" -> BsonInt64(size)
        )

      mongoSubDoc.append(manifestId, mongoSubDoc)
    }
    mongoSubDoc
  }

  def processObjectsS2MSI2A(dataObjects: List[JsValue], transformURL: String => (String, String)): BsonDocument = {
    var mongoSubDoc = BsonDocument()
    var imageryDoc = BsonArray()

    dataObjects.foreach { obj =>
      val manifestId = (obj \ "ID").as[String]
      val href = (obj \ "byteStream" \ "fileLocation" \ "href").as[String]
      val fileName = href.split("/").last
      val size = (obj \ "byteStream" \ "size").as[Long]
      val fileUrl = transformURL(href)._1

      if (manifestId.startsWith("IMG_DATA")) {
        //  IMG_DATA_Band_B09_60m_Tile1_Data -> [IMG, DATA, Band, B09, 60m, Tile1, Data]
        val splitId = manifestId.split("_")
        val band =
          if (splitId(3).startsWith("B0")) splitId(3).replace("B0", "")
          else if (splitId(3).startsWith("B1")) splitId(3).replace("B1", "1")
          else splitId(3)

        val resolution = splitId(4).replace("m", "").toInt

        imageryDoc.add(
          BsonDocument(
            "_id" -> BsonString(generateUUID()),
            "band" -> BsonString(band),
            "tile" -> BsonString(splitId(5).replace("Tile", "")),
            "resolution" -> BsonDocument(
              "x" -> resolution,
              "y" -> resolution,
            ),
            "url" -> BsonString(fileUrl),
            "status" -> BsonString("remote"),
            "fileName" -> BsonString(fileName),
            "size" -> BsonInt64(size)
          ))
      }
      else {
        val updatedMongoSubDoc =
            BsonDocument(
              "_id" -> BsonString(generateUUID()),
              "status" -> BsonString("remote"),
              "fileName" -> BsonString(fileName),
              "url" -> BsonString(fileUrl),
              "size" -> BsonInt64(size)
            )
        mongoSubDoc.append(manifestId, updatedMongoSubDoc)
      }
    }

    mongoSubDoc.append("imagery", imageryDoc)
    mongoSubDoc
  }

  def processObjectsS2MSI1C(dataObjects: List[JsValue], transformURL: String => (String, String)): BsonDocument = {
    var mongoSubDoc = BsonDocument()
    var imageryDoc = BsonArray()

    dataObjects.foreach { obj =>
      val manifestId = (obj \ "ID").as[String]
      val href = (obj \ "byteStream" \ "fileLocation" \ "href").as[String]
      val fileName = href.split("/").last
      val size = (obj \ "byteStream" \ "size").as[Long]
      val fileUrl = transformURL(href)._1

      if (manifestId.startsWith("IMG_DATA")) {
        //  IMG_DATA_Band_10m_1_Tile1_Data -> [IMG, DATA, Band, 10m, 1, Tile1, Data]
        var splitId = manifestId.split("_").toList

        if (manifestId.contains("TCI"))
          splitId = List("", "", "", "10m", "TCI", splitId(4))

        val resolution = splitId(3).replace("m", "").toInt

        imageryDoc.add(
          BsonDocument(
            "_id" -> BsonString(generateUUID()),
            "band" -> BsonString(splitId(4)),
            "tile" -> BsonString(splitId(5).replace("Tile", "")),
            "resolution" -> BsonDocument(
              "x" -> resolution,
              "y" -> resolution,
            ),
            "url" -> BsonString(fileUrl),
            "status" -> BsonString("remote"),
            "fileName" -> BsonString(fileName),
            "size" -> BsonInt64(size)
          ))
      }
      else {
        val updatedMongoSubDoc =
          BsonDocument(
            "_id" -> BsonString(generateUUID()),
            "status" -> BsonString("remote"),
            "fileName" -> BsonString(fileName),
            "url" -> BsonString(fileUrl),
            "size" -> BsonInt64(size)
          )
        mongoSubDoc.append(manifestId, updatedMongoSubDoc)
      }
    }

    mongoSubDoc.append("imagery", imageryDoc)
    mongoSubDoc
  }

  def processObjectsSentinel1(dataObjects: List[JsValue], transformURL: String => (String, String)): BsonDocument = {
    var mongoSubDoc = BsonDocument()
    var imageryDoc = BsonArray()

    dataObjects.foreach { obj =>
      val manifestId = (obj \ "ID").as[String]
      val href = (obj \ "byteStream" \ "fileLocation" \ "href").as[String]
      val fileName = href.split("/").last
      val size = (obj \ "byteStream" \ "size").as[Long]
      val fileUrl = transformURL(href)._1

      //  ./measurement/s1a-ew1-slc-hh-20190626t185351-20190626t185422-027854-03250b-001.tiff
      if (href.endsWith(".tiff")) {
        // s1a-ew1-slc-hh-20190626t185351-20190626t185422-027854-03250b-001

        val name = href.split("/")(2).replaceAll(".tiff", "").split("-")

        imageryDoc.add(
          BsonDocument(
            "_id" -> BsonString(generateUUID()),
            "polarisation" -> BsonString(name(3)),
            "swathId" -> BsonString(name(1)),
            "imageNumber" -> BsonString(name.last),
            "url" -> BsonString(fileUrl),
            "status" -> BsonString("remote"),
            "fileName" -> BsonString(fileName),
            "size" -> BsonInt64(size)
          ))
      }
      else {
        val updatedMongoSubDoc =
          BsonDocument(
            "_id" -> BsonString(generateUUID()),
            "status" -> BsonString("remote"),
            "fileName" -> BsonString(fileName),
            "url" -> BsonString(fileUrl),
            "size" -> BsonInt64(size)
          )
        mongoSubDoc.append(manifestId, updatedMongoSubDoc)
      }
    }

    mongoSubDoc.append("imagery", imageryDoc)
    mongoSubDoc
  }

  def processObjectsOL_1_ERR___(dataObjects: List[JsValue], transformURL: String => (String, String)): BsonDocument = {
    var mongoSubDoc = BsonDocument()
    var imageryDoc = BsonArray()

    dataObjects.foreach { obj =>
      val manifestId = (obj \ "ID").as[String]
      val href = (obj \ "byteStream" \ "fileLocation" \ "href").as[String]
      val fileName = href.split("/").last
      val size = (obj \ "byteStream" \ "size").as[Long]
      val fileUrl = transformURL(href)._1

      if (manifestId.endsWith("radianceData")) {
        //  Oa01_radianceUnit -> [Oa01, radianceUnit]
        val splitId = manifestId.split("_")

        imageryDoc.add(
          BsonDocument(
            "_id" -> BsonString(generateUUID()),
            "band" -> BsonString(splitId(0)),
            "resolution" -> BsonDocument(
              "x" -> 1200,
              "y" -> 1200,
            ),
            "fileName" -> BsonString(fileName),
            "url" -> BsonString(fileUrl),
            "status" -> BsonString("remote"),
            "size" -> BsonInt64(size)
          ))
      }
      else {
        val updatedMongoSubDoc =
          BsonDocument(
            "_id" -> BsonString(generateUUID()),
            "status" -> BsonString("remote"),
            "fileName" -> BsonString(fileName),
            "url" -> BsonString(fileUrl),
            "size" -> BsonInt64(size)
          )
        mongoSubDoc.append(manifestId, updatedMongoSubDoc)
      }
    }

    mongoSubDoc.append("imagery", imageryDoc)
    mongoSubDoc
  }


}

package utils

import java.nio.charset.StandardCharsets

import com.fasterxml.jackson.databind.node.ArrayNode
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
import com.jayway.jsonpath.{Configuration, JsonPath}
import mongo.MongoDAO
import net.minidev.json.JSONArray
import org.bson.BsonValue
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json.XML
import org.mongodb.scala.bson.{BsonArray, BsonBoolean, BsonDateTime, BsonDocument, BsonDouble, BsonInt64, BsonString}
import play.api.libs.json.{JsValue, Json}
import sources.{Extraction, Transformations}
import utils.Utils.{dateFormat, writeFile}

import scala.util.{Failure, Success, Try}

object ParsingUtils {
  val jsonConf: Configuration = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider()).build()

  def processExtractions(responseBytes: Array[Byte], extractions: List[Extraction],
                         productId: String, url: String, filename: String = ""): Either[Array[Byte], String] = {

    val response = parseFile(responseBytes, extractions)

    extractions.foreach { e =>

      try {
        e.queryType match {
          case "file" if response.isRight =>
            val destPath = e.destPath.replace("(productId)", productId)
              .replace("(filename)", filename)
              .replace("xml", "json")
            processFile(Right(response.right.get), e.copy(destPath = destPath), url, productId)
          case "multi-file" =>
            val destPath = e.destPath.replace("(productId)", productId)
            processMultiFile(response.right.get, e.copy(destPath = destPath), url, productId)
          case "single-value" =>
            processSingleValue(response.right.get, e, url, productId)
          case "multi-value" =>
            processMultiValue(response.right.get, e, url, productId)
          case _ =>
            val destPath = e.destPath
              .replace("(productId)", productId)
              .replace("(filename)", filename)

            processFile(Left(response.left.get), e.copy(destPath = destPath), url, productId)
        }
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }

    response
  }

  private def processFile(response: Either[Array[Byte], String],
                          extraction: Extraction,
                          url: String,
                          productId: String): Unit = {

    response match {
      case Left(responseBytes) =>
        val transformed = Transformations.transform(extraction, responseBytes).asInstanceOf[Array[Byte]]
        writeFile(extraction.destPath, transformed)
      case Right(doc) =>
        val node = Json.parse(JsonPath.using(jsonConf).parse(doc).read[Object](extraction.query).toString)
        val transformed = Transformations.transform(extraction, node).asInstanceOf[JsValue]
        writeFile(extraction.destPath, transformed.toString)
    }

    MongoDAO.addFieldToDoc(productId, extraction.metamodelMapping, BsonString(extraction.destPath), extraction.collection)

    val queryDoc = generateQueryJson(extraction, url, BsonString(extraction.destPath), productId)
    MongoDAO.insertDoc(queryDoc, MongoDAO.FETCHING_LOG_COL)
  }


  private def processMultiFile(docStr: String, extraction: Extraction, url: String, productId: String): Unit = {
    val result = JsonPath.read[JSONArray](docStr, extraction.query).toJSONString
    val resultJson = Json.parse(result).as[List[JsValue]]

    var destPaths = List[String]()
    resultJson.foreach { node =>
      val destPathI = extraction.destPath.replace("(i)", s"(${destPaths.size})")
      destPaths ::= destPathI
      val transformed = Transformations.transform(extraction, node).asInstanceOf[JsValue]
      writeFile(destPathI, transformed.toString)
    }

    val bsonDestPaths = BsonArray(destPaths.map(BsonString(_)))
    MongoDAO.addFieldToDoc(productId, extraction.metamodelMapping, bsonDestPaths, extraction.collection)

    val queryDoc = generateQueryJson(extraction, url, bsonDestPaths, productId)
    MongoDAO.insertDoc(queryDoc, MongoDAO.FETCHING_LOG_COL)

  }


  private def processSingleValue(docStr: String, extraction: Extraction, url: String, productId: String): Unit = {

    val value = extraction.resultType match {
      case "string" | "int" | "double" | "boolean" | "date" =>

        val result = Try(JsonPath.read[String](docStr, extraction.query).toString) match {
          case Failure(_) =>
            // metadataFields[?(@.fieldName=='Landsat Product Identifier')][0].value <- [][] impossible with this lib
            JsonPath.using(jsonConf).parse(docStr).read[ArrayNode](extraction.query).get(0).asText
          case Success(v) => v
        }

        extraction.resultType match {
          case "int" => result.toInt
          case "double" => result.toDouble
          case "boolean" => result.toBoolean
          case "date" => try {
            DateTimeFormat.forPattern(extraction.dtfStr).parseDateTime(result)
          } catch {
            case _: Exception =>
              println(extraction)
              println(result)
              null
          }
          case "string" => result
        }

      case _ =>
        val obj = JsonPath.using(jsonConf).parse(docStr).read[Object](extraction.query)
        Json.parse(obj.toString)
    }

    val transformedAny = Transformations.transform(extraction, value)
    val transformed: BsonValue = extraction.resultTypeAftrTransf match {
      case "int" => BsonInt64(transformedAny.asInstanceOf[Int])
      case "double" => BsonDouble(transformedAny.asInstanceOf[Double])
      case "boolean" => BsonBoolean(transformedAny.asInstanceOf[Boolean])
      case "string" => BsonString(transformedAny.asInstanceOf[String])
      case "date" => BsonDateTime(transformedAny.asInstanceOf[DateTime].toDate)
      case _ => BsonDocument(transformedAny.asInstanceOf[String])
    }

    MongoDAO.addFieldToDoc(productId, extraction.metamodelMapping, transformed, extraction.collection)

    val queryJson = generateQueryJson(extraction, url, transformed, productId)
    MongoDAO.insertDoc(queryJson, MongoDAO.FETCHING_LOG_COL)
  }


  private def generateQueryJson(extraction: Extraction, url: String, result: BsonValue, productId: String): BsonDocument = {
    BsonDocument(
      "query" -> BsonDocument(
        "productId" -> BsonString(productId),
        "name" -> BsonString(extraction.name),
        "date" -> BsonString(new DateTime().toString(dateFormat)),
        "url" -> BsonString(url),
        "expression" -> BsonString(extraction.query),
        "context" -> BsonString(extraction.context),
        "contextFormat" -> BsonString(extraction.contextFormat),
      ),
      "result" -> BsonDocument(
        "type" -> BsonString(extraction.resultType),
        "value" -> result
      ),
      "metamodelMapping" -> BsonString(extraction.metamodelMapping),
      "mongodbCollection" -> BsonString(extraction.collection),

    )

  }

  private def processMultiValue(doc: String, extraction: Extraction, url: String, productId: String): Unit = {

    val result = JsonPath.read[JSONArray](doc, extraction.query).toJSONString
    val resultJson = Json.parse(result).as[List[JsValue]]

    val resultsTyped = extraction.resultType match {
      case "int" => resultJson.map(_.as[Int])
      case "double" => resultJson.map(_.as[Double])
      case "boolean" => resultJson.map(_.as[Boolean])
      case "string" => resultJson.map(_.as[String])
      case "date" => DateTimeFormat.forPattern(extraction.dtfStr).parseDateTime(result)
      case _ => resultJson
    }

    val transformedAny = Transformations.transform(extraction, resultsTyped)

    val transformed: BsonValue = extraction.resultTypeAftrTransf match {
      case "int" => BsonArray(transformedAny.asInstanceOf[List[Int]])
      case "double" => BsonArray(transformedAny.asInstanceOf[List[Double]])
      case "boolean" => BsonArray(transformedAny.asInstanceOf[List[Boolean]])
      case "string" => BsonArray(transformedAny.asInstanceOf[List[String]])
      case "date" => BsonDateTime(transformedAny.asInstanceOf[DateTime].toDate)
      case _ =>
        val bsonDocumentList = transformedAny.asInstanceOf[List[JsValue]].map(jsv => BsonDocument(jsv.toString))
        BsonArray(bsonDocumentList)
    }

    MongoDAO.addFieldToDoc(productId, extraction.metamodelMapping, transformed, extraction.collection)

    val queryJson = generateQueryJson(extraction, url, transformed, productId)
    MongoDAO.insertDoc(queryJson, MongoDAO.FETCHING_LOG_COL)
  }


  private def parseFile(responseBytes: Array[Byte], extractions: List[Extraction]): Either[Array[Byte], String] = {

    extractions.head.contextFormat match {
      case "xml" =>
        Right(XML.toJSONObject(new String(responseBytes, StandardCharsets.UTF_8)).toString)
      case "json" =>
        Right(new String(responseBytes, StandardCharsets.UTF_8))
      case _ =>
        Left(responseBytes)
    }

  }

}

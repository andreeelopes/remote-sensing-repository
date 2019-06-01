package utils

import java.nio.charset.StandardCharsets

import com.fasterxml.jackson.databind.node.ArrayNode
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
import com.jayway.jsonpath.{Configuration, JsonPath}
import net.minidev.json.JSONArray
import org.joda.time.DateTime
import org.json.XML
import play.api.libs.json.{JsValue, Json}
import sources.{Extraction, Transformations}
import utils.Utils.{dateFormat, writeFile}

import scala.util.{Failure, Success, Try}

object ParsingUtils {
  val jsonConf = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider()).build()

  def processExtractions(responseBytes: Array[Byte], extractions: List[Extraction],
                         productId: String, url: String, filename: String = "") = {

    val response = parseFile(responseBytes, extractions)

    extractions.foreach { e =>
      e.queryType match {
        case "file" if response.isRight =>
          val destPathQuery = e.destPath
            .replace("(productId)", productId)
            .replace("(filename)", filename)
            .replace("xml", "json") //only applies to xml files

          processFile(response.right.get, e, destPathQuery, url)
        case "multi-file" =>
          val destPathQuery = e.destPath.replace("(productId)", productId)
          processMultiFile(response.right.get, e, destPathQuery, url)
        case "single-value" =>
          val destPathQuery = e.destPath.replace("(productId)", productId)
          processSingleValue(response.right.get, e, destPathQuery, url)
        case "multi-value" =>
          val destPathQuery = e.destPath
            .replace("(productId)", productId)
            .replace("xml", "json") //only applies to xml files

          processMultiValue(response.right.get, e, destPathQuery, url)
        case _ =>
          val destPathQuery = e.destPath
            .replace("(productId)", productId)
            .replace("(filename)", s"query-$filename")

          processFile(response.left.get, e, destPathQuery, url)
      }
    }

    response
  }

  private def processFile(responseBytes: Array[Byte],
                          extraction: Extraction,
                          destPathQuery: String,
                          url: String) = {
    val destPath = destPathQuery.replace("query-", "")

    val updatedExtraction = extraction.copy(destPath = destPath)
    val queryJson = generateQueryJsonFile(updatedExtraction, url)

    val transformed = Transformations.transform(extraction, responseBytes).right.get
    writeFile(destPath, transformed)
    writeFile(s"$destPathQuery.json", queryJson.toString)
  }

  private def processFile(doc: String, extraction: Extraction, destPathQuery: String, url: String) = {
    val destPath = destPathQuery.replace("query-", "")

    val updatedExtraction = extraction.copy(destPath = destPath)
    val queryJson = generateQueryJsonFile(updatedExtraction, url)

    val node = JsonPath.using(jsonConf).parse(doc).read[Object](extraction.query)

    val transformed = Transformations.transform(extraction, node).left.get

    writeFile(destPath, transformed.head)
    writeFile(destPathQuery, queryJson.toString)
  }

  private def generateQueryJsonFile(extraction: Extraction, url: String) = {
    generateQueryJson(extraction, url, s""""value" : "${extraction.destPath}"""")
  }

  private def processMultiFile(docStr: String, extraction: Extraction, destPathQuery: String, url: String) = {
    val result = JsonPath.read[JSONArray](docStr, extraction.query).toJSONString
    val resultJson = Json.parse(result).as[List[JsValue]]

    var destPaths = List[String]()
    resultJson.foreach { node =>
      val destPathI = destPathQuery.replace("query-", s"(${destPaths.size})-")
      destPaths ::= destPathI
      val transformed = Transformations.transform(extraction, node).left.get
      writeFile(destPathI, transformed.head)
    }

    val queryJson = generateQueryJsonMultiFile(extraction, destPaths, url)

    writeFile(destPathQuery, queryJson.toString)

  }

  private def generateQueryJsonMultiFile(extraction: Extraction, destPaths: List[String], url: String) = {

    val fileLocations = destPaths.map(s => s""""$s"""").mkString(",")
    val jsonFragment = s""""value" : [$fileLocations]"""

    generateQueryJson(extraction, url, jsonFragment)
  }

  private def processSingleValue(docStr: String, extraction: Extraction, destPathQuery: String, url: String) = {

    val value = extraction.resultType match {
      case "string" | "int" | "double" | "boolean" =>

        Try(JsonPath.read[String](docStr, extraction.query).toString) match {
          case Failure(_) =>
            // metadataFields[?(@.fieldName=='Landsat Product Identifier')][0].value <- [][] impossible with this lib
            JsonPath.using(jsonConf).parse(docStr).read[ArrayNode](extraction.query).get(0).asText
          case Success(v) => v
        }
      case _ =>
        val conf = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider()).build()

        JsonPath.using(conf).parse(docStr).read[Object](extraction.query).toString
    }

    val transformed = Transformations.transform(extraction, value).left.get.head

    val queryJson = generateQueryJsonSingleValue(extraction, transformed, url)
    writeFile(destPathQuery, queryJson.toString)

  }

  private def generateQueryJsonSingleValue(extraction: Extraction, value: String, url: String) = {
    val auxValue = if (extraction.resultTypeAftrTransf == "string") s""""$value"""" else value

    generateQueryJson(extraction, url, s""""value" : $auxValue""")
  }

  private def generateQueryJson(extraction: Extraction, url: String, result: String) = {
    val adaptedQuotesUrl = url.replace("\"", "\\\"")

    try {
      Json.parse(
        s"""
    {
      "query" : {
        "name" : "${extraction.name}",
        "date" : "${new DateTime().toString(dateFormat)}",
        "url" : "$adaptedQuotesUrl",
        "expression" : "${extraction.query}",
        "context" : "${extraction.context}",
        "context-format" : "${extraction.contextFormat}"
      },
      "result" : {
        "type" : "${extraction.resultType}",
        $result
      },
      "metamodel-mapping" : "${extraction.metamodelMapping}"
    }"""
      )
    } catch {
      case e: Exception => e.printStackTrace()
        println(result)
        println("####")
        ""
    }


  }

  private def processMultiValue(doc: String, extraction: Extraction, destPathQuery: String, url: String) = {

    val result = JsonPath.read[JSONArray](doc, extraction.query).toJSONString
    val resultJson = Json.parse(result).as[List[JsValue]]
      .map(node => node.toString())

    val resultJsonStr = if (extraction.resultTypeAftrTransf == "string") resultJson.map(nodeStr => s""""$nodeStr"""") else resultJson

    val transformed = Transformations.transform(extraction, resultJsonStr).left.get.mkString(",")

    val queryJson = generateQueryJsonMultiValue(extraction, transformed, url)

    writeFile(destPathQuery, queryJson.toString)
  }

  private def generateQueryJsonMultiValue(extraction: Extraction, values: String, url: String) = {
    val jsonFragment = s""""value" : [$values]"""

    generateQueryJson(extraction, url, jsonFragment)
  }

  private def parseFile(responseBytes: Array[Byte], extractions: List[Extraction]) = {


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

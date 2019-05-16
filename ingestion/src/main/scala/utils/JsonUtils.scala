package utils

import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
import com.jayway.jsonpath.{Configuration, JsonPath}
import net.minidev.json.JSONArray
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import sources.ExtractionEntry
import utils.Utils.{dateFormat, writeFile}

object JsonUtils {

  //TODO .json no final

  def processFile(doc: String, extractionEntry: ExtractionEntry, productId: String, filename: String, url: String) = {
    val destPath = extractionEntry.destPath
      .replace("(productId)", productId)
      .replace("(filename)", filename)

    val updatedExtrEntry = extractionEntry.copy(destPath = destPath)
    val queryJson = generateQueryJsonFile(updatedExtrEntry, url)

    val node = JsonPath.read[Object](doc, extractionEntry.query).toString

    writeFile(destPath, node)
    writeFile(s"$destPath-query", queryJson.toString)
  }

  def generateQueryJsonFile(extractionEntry: ExtractionEntry, url: String) = {
    generateQueryJson(extractionEntry, url, s""""value" : "${extractionEntry.destPath}"""")

  }

  def processMultiFile(docStr: String, extractionEntry: ExtractionEntry, productId: String, url: String) = {
    val destPath = extractionEntry.destPath
      .replace("(productId)", productId)


    val result = JsonPath.read[JSONArray](docStr, extractionEntry.query).toJSONString
    val resultJson = Json.parse(result).as[List[JsValue]]

    var destPaths = List[String]()
    resultJson.foreach { node =>
      val destPathI = s"$destPath-(${destPaths.size})"
      destPaths ::= destPathI
      writeFile(destPathI, node.toString)
    }

    val queryJson = generateQueryJsonMultiFile(extractionEntry, destPaths, url)

    writeFile(s"$destPath-query", queryJson.toString)

  }

  def generateQueryJsonMultiFile(extractionEntry: ExtractionEntry, destPaths: List[String], url: String) = {

    val fileLocations = destPaths.map(s => s""""$s"""").mkString(",")
    val jsonFragment = s""""value" : [$fileLocations]"""

    generateQueryJson(extractionEntry, url, jsonFragment)

  }

  def processSingleValue(docStr: String, extractionEntry: ExtractionEntry, productId: String, url: String) = {
    val queryFilePath = extractionEntry.destPath.replace("(productId)", productId)

    val value = extractionEntry.resultType match {
      case "string" => JsonPath.read[String](docStr, extractionEntry.query).toString
      case "int" => JsonPath.read[Int](docStr, extractionEntry.query).toString
      case "float" => JsonPath.read[Float](docStr, extractionEntry.query).toString
      case "boolean" => JsonPath.read[Boolean](docStr, extractionEntry.query).toString
      case _ =>
        val conf = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider()).build()

        JsonPath.using(conf).parse(docStr).read[Object]("$.features[0].properties.keywords[0]").toString
    }

    val queryJson = generateQueryJsonSingleValue(extractionEntry, value, url)
    writeFile(s"$queryFilePath-query", queryJson.toString)

  }

  def generateQueryJsonSingleValue(extractionEntry: ExtractionEntry, value: String, url: String) = {
    val auxValue = if (extractionEntry.resultType == "string") s""""$value"""" else value

    generateQueryJson(extractionEntry, url, s""""value" : $auxValue""")
  }

  def generateQueryJson(extractionEntry: ExtractionEntry, url: String, result: String) = {
    Json.parse(
      s"""
    {
      "query" : {
        "name" : "${extractionEntry.name}",
        "date" : "${new DateTime().toString(dateFormat)}",
        "url" : "$url",
        "expression" : "${extractionEntry.query}",
        "docContext" : "${extractionEntry.docContext}"
      },
      "result" : {
        "type" : "${extractionEntry.resultType}",
        $result
      }
    }"""
    )

  }

  def processMultiValue(doc: String, extractionEntry: ExtractionEntry, productId: String, url: String) = {
    val destPath = extractionEntry.destPath.replace("(productId)", productId)

    val result = JsonPath.read[JSONArray](doc, extractionEntry.query).toJSONString
    val resultJson = Json.parse(result).as[List[JsValue]].map(node => node.toString())

    val queryJson = generateQueryJsonMultiValue(extractionEntry, resultJson, url)
    writeFile(s"$destPath-query", queryJson.toString)
  }

  def generateQueryJsonMultiValue(extractionEntry: ExtractionEntry, values: List[String], url: String) = {

    val multiValues = values.mkString(",")

    val jsonFragment = s""""value" : [$multiValues]"""

    generateQueryJson(extractionEntry, url, jsonFragment)

  }


}

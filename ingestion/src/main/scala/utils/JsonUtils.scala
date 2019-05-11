package utils

import com.jayway.jsonpath.JsonPath
import net.minidev.json.JSONArray
import sources.ExtractionEntry
import utils.Utils.writeFile
import utils.XmlUtils.{generateQueryXmlFile, generateQueryXmlMultiFile, generateQueryXmlMultiValue, generateQueryXmlSingleValue}

import scala.xml.XML

object JsonUtils {

  def processSingleValue(docStr: String, extractionEntry: ExtractionEntry, productId: String, url: String) = {
    val queryFilePath = extractionEntry.destPath.replace("(productId)", productId)

    val value = JsonPath.read[String](docStr, extractionEntry.query)
    val queryXML = generateQueryXmlSingleValue(extractionEntry, value, url)
    XML.save(s"$queryFilePath-query", queryXML)
  }


  def processMultiFile(docStr: String, extractionEntry: ExtractionEntry, productId: String, url: String) = {
    val destPath = extractionEntry.destPath
      .replace("(productId)", productId)


    val result = JsonPath.read[JSONArray](docStr, extractionEntry.query)

    var destPaths = List[String]()
    for (i <- 0 until result.size()) {
      val node = result.get(i)
      val destPathI = s"$destPath-($i)"
      destPaths ::= destPathI
      writeFile(destPathI, node.toString)
    }

    val queryXml = generateQueryXmlMultiFile(extractionEntry, destPaths, url)

    XML.save(s"$destPath-query", queryXml)

  }

  def processMultiValue(doc: String, extractionEntry: ExtractionEntry, productId: String, url: String) = {
    val destPath = extractionEntry.destPath.replace("(productId)", productId)

    val result = JsonPath.read[JSONArray](doc, extractionEntry.query)

    var values = List[String]()
    for (i <- 0 until result.size)
      values ::= result.get(i).toString

    val queryXml = generateQueryXmlMultiValue(extractionEntry, values, url)
    XML.save(s"$destPath-query", queryXml)
  }


  def processFile(doc: String, extractionEntry: ExtractionEntry, productId: String, filename: String, url: String) = {
    val destPath = extractionEntry.destPath
      .replace("(productId)", productId)
      .replace("(filename)", filename)

    val updatedExtrEntry = extractionEntry.copy(destPath = destPath)
    val queryXML = generateQueryXmlFile(updatedExtrEntry, url)

    val node = JsonPath.read[Object](doc, extractionEntry.query).toString

    writeFile(destPath, node)
    XML.save(s"$destPath-query", queryXML)
  }

}

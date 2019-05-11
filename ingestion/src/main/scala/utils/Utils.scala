package utils

import java.io.{BufferedOutputStream, File, FileOutputStream, PrintWriter}
import java.util.UUID

import com.typesafe.config.Config
import sources.ExtractionEntry
import utils.XmlUtils.generateQueryXmlFile

import scala.collection.JavaConverters._
import scala.xml.XML


trait KryoSerializable

object Utils {

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

  def generateUUID(): String = UUID.randomUUID().toString

  def processFile(responseBytes: Array[Byte],
                  extractionEntry: ExtractionEntry,
                  productId: String,
                  filename: String,
                  url: String) = {
    val destPath = extractionEntry.destPath
      .replace("(productId)", productId)
      .replace("(filename)", filename)

    val updatedExtrEntry = extractionEntry.copy(destPath = destPath)
    val queryXML = generateQueryXmlFile(updatedExtrEntry, url)

    writeFile(destPath, responseBytes)
    XML.save(s"$destPath-query", queryXML)
  }

  def writeFile(filename: String, data: Array[Byte]) = {
    val bos = new BufferedOutputStream(new FileOutputStream(filename))
    Stream.continually(bos.write(data))
    bos.close()
  }

  def writeFile(filename: String, content: String) = {
    val pw = new PrintWriter(new File(filename))
    pw.write(content)
    pw.close()
  }

  def getExtractions(config: Config, configName: String) = {

    config.getConfigList(s"sources.$configName.extractions").asScala.toList.map { entry =>
      ExtractionEntry(
        entry.getString("name"),
        entry.getString("query-type"),
        entry.getString("result-type"),
        entry.getString("query"),
        entry.getString("parent-extraction"),
        entry.getString("dest-path"),
        entry.getString("api")
      )
    }
  }


}

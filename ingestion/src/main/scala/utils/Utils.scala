package utils

import java.io.{BufferedOutputStream, File, FileOutputStream, PrintWriter}
import java.util.UUID

import com.typesafe.config.Config
import sources.Extraction

import scala.collection.JavaConverters._
import scala.util.Try


trait KryoSerializable


object Utils {

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

  def generateUUID(): String = UUID.randomUUID().toString

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

  def getAllExtractions(config: Config, configName: String, program: String = "-1", platform: String = "-1", productType: String = "-1") = {
    val programExt = Try(getExtractions(config, s"$configName.$program")).getOrElse(List())
    val platformExt = Try(getExtractions(config, s"$configName.$platform")).getOrElse(List())
    val productTypeSpecificExt = Try(getExtractions(config, s"$configName.$productType")).getOrElse(List())

    getExtractions(config, s"$configName") ::: programExt ::: platformExt ::: productTypeSpecificExt
  }

  def getExtractions(config: Config, configName: String) = {

    config.getConfigList(s"sources.$configName.extractions").asScala.toList.map { entry =>
      Extraction(
        entry.getString("name"),
        entry.getString("query-type"),
        entry.getString("result-type"),
        entry.getString("query"),
        entry.getString("context"),
        entry.getString("dest-path"),
        entry.getString("context-format")
      )
    }
  }


  def productsToFetch(config: Config, configName: String) = {

    config.getConfigList(s"sources.$configName.products").asScala.toList.map { entry =>
      ProductEntry(
        entry.getString("program"),
        entry.getString("platform"),
        entry.getStringList("product-type").asScala.toList
      )
    }
  }


}

case class ProductEntry(program: String, platform: String, productType: List[String])

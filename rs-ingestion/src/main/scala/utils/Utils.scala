package utils

import java.io.{BufferedOutputStream, File, FileOutputStream, PrintWriter}
import java.util
import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import mongo.MongoDAO
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import sources.Extraction

import scala.collection.JavaConverters._
import scala.util.Try


trait KryoSerializable


object Utils {

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

  val config: Config = ConfigFactory.load()

  def generateUUID(): String = UUID.randomUUID().toString

  def writeFile(filename: String, data: Array[Byte]): Unit = {
    val bos = new BufferedOutputStream(new FileOutputStream(filename))
    Stream.continually(bos.write(data))
    bos.close()
  }

  def writeFile(filename: String, content: String): Unit = {
    val pw = new PrintWriter(new File(filename))
    pw.write(content)
    pw.close()
  }

  def getAllExtractions(configName: String, program: String = "-1", platform: String = "-1", productType: String = "-1"): List[Extraction] = {
    val programExt = Try(getExtractions(s"$configName.$program", program)).getOrElse(List())
    val platformExt = Try(getExtractions(s"$configName.$platform", platform)).getOrElse(List())
    val productTypeSpecificExt = Try(getExtractions(s"$configName.$productType", productType)).getOrElse(List())

    getExtractions(s"$configName", MongoDAO.PRODUCTS_COL) ::: programExt ::: platformExt ::: productTypeSpecificExt
  }

  def getExtractions(configName: String, collection: String): List[Extraction] = {

    config.getConfigList(s"sources.$configName.extractions").asScala.toList.map { entry =>
      Extraction(
        entry.getString("name"),
        entry.getString("query-type"),
        entry.getString("result-type"),
        Try(entry.getString("result-type-transformation")).getOrElse(entry.getString("result-type")),
        entry.getString("query"),
        entry.getString("context"),
        Try(entry.getString("dest-path")).getOrElse(""),
        entry.getString("context-format"),
        entry.getString("metamodel-mapping"),
        collection,
        Try(entry.getString("date-format")).getOrElse(null),
        Try(entry.getBoolean("update-url")).getOrElse(false),
      )
    }
  }


  def getIndexesConf(configName: String): util.List[Index] = {
    config.getConfigList(configName).asScala.toList.map {
      entry =>
        Index(
          entry.getString("type"),
          entry.getString("order"),
          entry.getStringList("fields-names").asScala.toList
        )
    }.asJava
  }


  case class Index(indexType: String, order: String, fields: List[String])

  def productsToFetch(configName: String): List[ProductEntry] = {
    config.getConfigList(s"sources.$configName.products").asScala.toList.map { entry =>
      ProductEntry(
        entry.getString("program"),
        entry.getString("platform"),
        entry.getStringList("product-type").asScala.toList
      )
    }
  }


  case class ProductEntry(program: String, platform: String, productType: List[String])


  def sourceConcurrencyLimits(): Map[String, Int] = {
    val sourcesConfig = config.getObject("sources")
    var concurrencyLimits: Map[String, Int] = Map()

    sourcesConfig.asScala.foreach { case (k, _) =>
      val sourceConfig = config.getConfig(s"sources.$k")
      val limit = Try(sourceConfig.getInt("concurrency-limit")).getOrElse(Int.MaxValue)

      concurrencyLimits += (k -> limit)
    }

    concurrencyLimits
  }

}
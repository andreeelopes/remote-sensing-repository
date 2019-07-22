package utils

import java.io.{BufferedOutputStream, File, FileOutputStream, PrintWriter}
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}
import mongo.MongoDAO
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import sources.Extraction
import scala.concurrent.duration._

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}
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

    val sourcesJson = Json.parse(MongoDAO.getDoc("sources", MongoDAO.SOURCES_COL).get.toJson)

    val pathElems = List("sources") ::: configName.split("\\.").toList ::: List("extractions")

    val extractions =
      pathElems
        .foldLeft(sourcesJson)((js, elem) => (js \ elem).as[JsValue])
        .as[JsArray].value.toList


    extractions.map { entry =>
      Extraction(
        (entry \ "name").as[String],
        (entry \ "query-type").as[String],
        (entry \ "result-type").as[String],
        Try((entry \ "result-type-transformation").as[String]).getOrElse((entry \ "result-type").as[String]),
        (entry \ "query").as[String],
        (entry \ "context").as[String],
        Try((entry \ "dest-path").as[String]).getOrElse(""),
        (entry \ "context-format").as[String],
        (entry \ "metamodel-mapping").as[String],
        collection,
        Try((entry \ "date-format").as[String]).getOrElse(null),
        Try((entry \ "update-url").as[Boolean]).getOrElse(false),
      )
    }
  }


  def extractIndexes(indexesJson: JsArray): List[Index] = {

    indexesJson.value.toList.map { entry =>
      Index(
        (entry \ "type").as[String],
        (entry \ "order").as[String],
        (entry \ "fields-names").as[JsArray].value.map(fn => fn.as[String]).toList,
      )
    }
  }


  case class Index(indexType: String, order: String, fields: List[String])

  def productsToFetch(configName: String): List[ProductEntry] = {
    val productsArray = (MongoDAO.sourcesJson \ configName \ "products").as[JsArray].value.toList

    productsArray.map { entry =>
      ProductEntry(
        (entry \ "program").as[String],
        (entry \ "platform").as[String],
        (entry \ "product-type").as[List[JsValue]].map(p => (p \ "name").as[String])
      )
    }
  }

  case class ProductConf(name: String, epoch: Int, fetchingFrequency: FiniteDuration, fetchingProvider: String)


  def getProductConf(configName: String, program: String, platform: String, productType: String): ProductConf = {
    val productsArray = (MongoDAO.sourcesJson \ configName \ "products").as[JsArray].value.toList

    val p = (productsArray
      .filter(p => (p \ "program").as[String] == program && (p \ "platform").as[String] == platform)
      .head \ "product-type").as[List[JsValue]]
      .filter(pt => (pt \ "name").as[String] == productType)
      .head

    val name = (p \ "name").as[String]
    val epoch = (p \ "epoch-years").as[Int]
    val fetchingFrequency = Duration((p \ "fetching-frequency").as[Long], TimeUnit.SECONDS).toSeconds.seconds
    val fetchingProvider = (p \ "provider").as[String]
    ProductConf(name, epoch, fetchingFrequency, fetchingProvider)
  }

  case class ProductEntry(program: String, platform: String, productType: List[String])


  def sourceConcurrencyLimits(): Map[String, Int] = {
    val sourcesJson = MongoDAO.sourcesJson.as[JsObject].value.toList

    var concurrencyLimits: Map[String, Int] = Map()

    sourcesJson.foreach {
      case (k, json) =>
        val limit = Try((json \ "concurrency-limit").as[Int]).getOrElse(Int.MaxValue)
        concurrencyLimits += (k -> limit)
    }

    concurrencyLimits
  }

  def getBandsEE: Map[String, List[(String, Int, Int)]] = {
    val sourcesJson = MongoDAO.sourcesJson.as[JsValue]

    var bands = Map[String, List[(String, Int, Int)]]()

    val sourceProducts = (sourcesJson \ "earth-explorer").as[JsObject].value.toList

    sourceProducts.foreach {
      p =>
        val bandsProductOpt = Try((p._2 \ "bands").as[JsArray]).toOption

        if (bandsProductOpt.isDefined) {
          val bandsProduct = bandsProductOpt.get
          var bandsList = List[(String, Int, Int)]()

          bandsProduct.value.foreach {
            b =>
              val name = (b \ "name").as[String]
              val resolutionX = (b \ "resolution" \ "y").as[Int]
              val resolutionY = (b \ "resolution" \ "y").as[Int]
              bandsList ::= (name, resolutionX, resolutionY)
          }
          bands += (p._1 -> bandsList)
        }
    }
    bands
  }

}
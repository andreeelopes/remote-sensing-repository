package sources

import java.io.File
import java.nio.charset.StandardCharsets

import com.typesafe.config.Config
import org.joda.time.DateTime
import org.json.XML
import org.mongodb.scala.bson.BsonDocument
import play.api.libs.json.{JsObject, JsValue, Json}
import sources.handlers.AuthConfig
import sources.handlers.Parsing.processExtractions
import utils.Utils._

import scala.util.{Failure, Success, Try}

class CopernicusOSearchSource(config: Config,
                              override val program: String,
                              override val platform: String,
                              override val productType: String)
  extends ProviderPeriodicRESTSource("copernicus-oah-opensearch", config, program, platform, productType) {

  override val configName = "copernicus-oah-opensearch"
  override val PROVIDER: String = "copernicus"

  val authConfigOpt = Some(AuthConfig(configName, config))
  val epochWork: CopernicusOSearchWork = generateWork(historyDates, isEpoch = true)
  val periodicInitialWork: CopernicusOSearchWork = generateWork(initial)

  override def generateWork(intervalDates: (DateTime, DateTime), isEpoch: Boolean): CopernicusOSearchWork = {
    new CopernicusOSearchWork(this, intervalDates, isEpoch)
  }

}


class CopernicusOSearchWork(override val source: CopernicusOSearchSource,
                            override val intervalDates: (DateTime, DateTime),
                            override val isEpoch: Boolean = false,
                            override val pageStart: Int = 0)
  extends ProviderPeriodicRESTWork(source, intervalDates, isEpoch, pageStart) {

  override val url: String = s"${source.baseUrl}start=$pageStart&rows=${source.pageSize}&" +
    s"q=endposition:[${intervalDates._1.toString(dateFormat)}%20TO%20${intervalDates._2.toString(dateFormat)}]" +
    s"%20AND%20producttype:${source.productType}"


  override def process(responseBytes: Array[Byte]): List[Work] = {
    val docJson = XML.toJSONObject(new String(responseBytes, StandardCharsets.UTF_8)).toString
    val doc = Json.parse(docJson)
    var workToBeDone = List[Work]()

    //    getNextPagesWork(doc).foreach(w => workToBeDone ::= w)

    val entriesOpt = Try((doc \ "feed" \ "entry").as[List[JsObject]])
    entriesOpt match {
      case Failure(_) => // there are no products in this time interval
      case Success(entries) => entries.headOption.foreach(entry => workToBeDone :::= processEntry(entry))
    }

    saveFetchingLog(BsonDocument(docJson))

    workToBeDone
  }


  private def processEntry(node: JsValue) = {

    val productId = (node \ "id").as[String]
    val title = (node \ "title").as[String]
    new File(s"${source.baseDir}/$productId").mkdirs()

    setupEntryMongo(productId)

    val auxExt = source.extractions.map(e => e.copy(contextFormat = "json"))

    processExtractions(node.toString.getBytes(StandardCharsets.UTF_8), auxExt, productId, url)

    List(new CopernicusManifestWork(
      new CopernicusManifestSource(source.config, source.program, source.platform, source.productType),
      productId,
      title)
    )

  }



  override def getNextPagesWork(doc: JsValue): Option[Work] = {
    val linksPages = (doc \ "feed" \ "link").as[List[JsValue]]
    val linkNextPage = linksPages.filter(link => (link \ "rel").as[String] == "next")

    if (linkNextPage.nonEmpty)
      Some(new CopernicusOSearchWork(source, intervalDates, isEpoch, pageStart + source.pageSize))
    else
      None
  }

  def generatePeriodicWork(): CopernicusOSearchWork = {
    val updatedIngestionWindow = source.adjustWindow(intervalDates)
    source.generateWork(updatedIngestionWindow)
  }

}

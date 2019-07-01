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
import sources.handlers.ErrorHandlers._

class CopernicusOSearchSource(config: Config,
                              override val program: String,
                              override val platform: String,
                              override val productType: String)
  extends ProviderPeriodicRESTSource("copernicus-oah-opensearch", config, program, platform, productType) {

  override val configName = "copernicus-oah-opensearch"
  override val PROVIDER: String = "copernicus"

  val authConfigOpt = Some(AuthConfig(configName, config))
  val epochInitialWork = new CopernicusOSearchWork(this, ingestionHistoryDates, isEpoch = true)
  val periodicInitialWork = new CopernicusOSearchWork(this, initialIngestion)

}


class CopernicusOSearchWork(override val source: CopernicusOSearchSource,
                            override val ingestionDates: (DateTime, DateTime),
                            override val isEpoch: Boolean = false,
                            override val pageStart: Int = 0)
  extends ProviderPeriodicRESTWork(source, ingestionDates, isEpoch, pageStart) {

  override val url: String = s"${source.baseUrl}start=$pageStart&rows=${source.pageSize}&" +
    s"q=endposition:[${ingestionDates._1.toString(dateFormat)}%20TO%20${ingestionDates._2.toString(dateFormat)}]" +
    s"%20AND%20producttype:${source.productType}"


  def generatePeriodicWork(): CopernicusOSearchWork = {
    val updatedIngestionWindow = source.adjustIngestionWindow(ingestionDates)
    new CopernicusOSearchWork(source, updatedIngestionWindow)
  }

  override def process(responseBytes: Array[Byte]): List[Work] = {
    val docJson = XML.toJSONObject(new String(responseBytes, StandardCharsets.UTF_8)).toString
    val doc = Json.parse(docJson)
    var workToBeDone = List[Work]()

//    getNextPagesWork(doc).foreach(w => workToBeDone ::= w)

    (doc \ "feed" \ "entry").as[List[JsObject]].headOption.foreach(entry => workToBeDone :::= processEntry(entry))

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

    //    generateCreodiasWork(productId, title) ::: TODO
    List(new CopernicusManifestWork(
      new CopernicusManifestSource(source.config, source.program, source.platform, source.productType),
      productId,
      title)
    )

  }

  private def generateCreodiasWork(productId: String, title: String) = {
    val creodiasConfigName = "creodias-odata"
    val creodiasBaseUrl = source.config.getString(s"sources.$creodiasConfigName.base-url")
    val creodiasUrl = s"$creodiasBaseUrl/${source.platform.capitalize}/search.json?maxRecords=1&productIdentifier=%$title%&status=all"

    val creodiasExt =
      getAllExtractions(creodiasConfigName, source.program, source.platform, source.productType)

    if (creodiasExt.nonEmpty)
      List(new ExtractionWork(new ExtractionSource(source.config, creodiasConfigName, creodiasExt, creodiasErrorHandler),
        creodiasUrl, productId))
    else
      List()
  }


  override def getNextPagesWork(doc: JsValue): Option[Work] = {
    val linksPages = (doc \ "feed" \ "link").as[List[JsValue]]
    val linkNextPage = linksPages.filter(link => (link \ "rel").as[String] == "next")

    if (linkNextPage.nonEmpty)
      Some(new CopernicusOSearchWork(source, ingestionDates, isEpoch, pageStart + source.pageSize))
    else
      None
  }

}

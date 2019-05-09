package sources

import akka.actor.ActorContext
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import javax.xml.xpath.XPathConstants
import org.w3c.dom.NodeList
import protocol.worker.WorkExecutor.WorkComplete
import utils.AkkaHTTP
import utils.Utils._
import utils.XmlUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.xml.XML


class CopernicusODataSource(configName: String, config: Config, val extractions: List[ExtractionEntry])
  extends Source(configName, config) with AuthComponent {

  override val authConfigOpt = Some(AuthConfig(configName, config))
}

class CopernicusODataWork(override val source: CopernicusODataSource, val url: String, val productId: String, filename: String)
  extends Work(source) { // TODO variable timeout depending on the size of the object


  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {

    implicit val origSender = context.sender

    AkkaHTTP.singleRequest(url, source.authConfigOpt).onComplete {
      case Success(response) =>

        if (response.status == StatusCode.int2StatusCode(500)) {
          response.discardEntityBytes()
          val e = new Exception("500 - Internal server error of source")
          context.self ! e
          throw e
        }

        Unmarshal(response.entity).to[String].onComplete {
          case Success(responseString) =>

            process(responseString)

            origSender ! WorkComplete(List())

          case Failure(e) => context.self ! e
            throw new Exception(e)
        }
      case Failure(e) => context.self ! e
        throw new Exception(e)

    }

  }


  private def process(resource: String) = {
    source.extractions.foreach { e =>
      e.queryType match {
        case "file" => processFile(resource, e)
        case "multi-file" => processMultiFile(resource, e)
        case "single-value" => processSingleValue(resource, e)
        case "multi-value" => processMultiValue(resource, e)
      }
    }
  }

  private def processFile(doc: String, extractionEntry: ExtractionEntry) = {
    val destPath = extractionEntry.destPath.replace("(productId)", productId).replace("(filename)", filename)
    val destPathQuery = generateQueryFilename(destPath)

    val updatedExtrEntry = extractionEntry.copy(destPath = destPath)
    val queryXML = generateQueryXmlFile(updatedExtrEntry, url)

    writeFile(destPath, doc)
    XML.save(destPathQuery, queryXML)
  }

  private def processSingleValue(doc: String, extractionEntry: ExtractionEntry) = {
    val queryFilePath = generateQueryFilename(extractionEntry.destPath.replace("(productId)", productId))

    val value = xPathQuery(doc, extractionEntry.path, XPathConstants.STRING).asInstanceOf[String]

    val queryXML = generateQueryXmlSingleValue(extractionEntry, value, url)

    XML.save(queryFilePath, queryXML)
  }


  private def processMultiValue(doc: String, extractionEntry: ExtractionEntry) = {
    val destPath = extractionEntry.destPath.replace("(productId)", productId)

    val nodeList = xPathQuery(doc, extractionEntry.path, XPathConstants.NODESET).asInstanceOf[NodeList]

    var values = List[String]()
    for (i <- 0 until nodeList.getLength) {
      val node = nodeList.item(i)
      values ::= xPathQuery(node, "/", XPathConstants.STRING).asInstanceOf[String]
    }

    val queryXml = generateQueryXmlMultiValue(extractionEntry, values, url)
    XML.save(destPath, queryXml)
  }


  private def processMultiFile(doc: String, extractionEntry: ExtractionEntry) = {
    val destPath = extractionEntry.destPath
      .replace("(productId)", productId)
      .replace("(i)", "-query")

    val nodeList = xPathQuery(doc, extractionEntry.path, XPathConstants.NODESET).asInstanceOf[NodeList]

    var destPaths = List[String]()
    for (i <- 0 until nodeList.getLength) {
      val node = nodeList.item(i)
      val destPathI = destPath.replace("-query", s"($i)")
      destPaths ::= destPathI
      writeFile(destPathI, nodeToString(node))
    }

    val queryXml = generateQueryXmlMultiFile(extractionEntry, destPaths, url)

    XML.save(destPath, queryXml)

  }


}



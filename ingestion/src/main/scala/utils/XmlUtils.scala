package utils

import java.io.{StringReader, StringWriter}

import com.typesafe.config.Config
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.{OutputKeys, TransformerException, TransformerFactory}
import javax.xml.xpath.XPathFactory
import org.joda.time.DateTime
import org.w3c.dom.Node
import org.xml.sax.InputSource
import sources.ExtractionEntry
import utils.Utils.dateFormat

import scala.collection.JavaConverters._

object XmlUtils {

  def xPathQuery(docStr: String, query: String, qName: QName) = {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val doc = builder.parse(new InputSource(new StringReader(docStr)))
    val xPathfactory = XPathFactory.newInstance()
    val xpath = xPathfactory.newXPath()
    val expr = xpath.compile(query)

    expr.evaluate(doc, qName)
  }

  def xPathQuery(node: Node, query: String, qName: QName) = {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val doc = builder.parse(new InputSource(new StringReader(nodeToString(node))))
    val xPathfactory = XPathFactory.newInstance()
    val xpath = xPathfactory.newXPath()
    val expr = xpath.compile(query)

    expr.evaluate(doc, qName)
  }

  def nodeToString(node: Node) = {
    val sw = new StringWriter
    try {
      val t = TransformerFactory.newInstance.newTransformer
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
      t.transform(new DOMSource(node), new StreamResult(sw))
    } catch {
      case te: TransformerException =>
        System.out.println("nodeToString Transformer Exception")
    }
    sw.toString
  }

  def getExtractions(config: Config, configName: String) = {

    config.getConfigList(s"sources.$configName.extractions").asScala.toList.map { entry =>
      ExtractionEntry(
        entry.getString("name"),
        entry.getString("query-type"),
        entry.getString("result-type"),
        entry.getString("path"),
        entry.getString("parent-extraction"),
        entry.getString("dest-path")
      )
    }
  }

  def generateQueryFilename(filename: String) = s"$filename-query.xml"


  def generateQueryXmlFile(extractionEntry: ExtractionEntry, url: String) = {
    <fetch name={extractionEntry.name}>
      <query date={new DateTime().toString(dateFormat)} type={extractionEntry.queryType}>
        <url>
          {url}
        </url>
        <path>
          {extractionEntry.path}
        </path>
        <parentExtraction>
          {extractionEntry.parentExtraction}
        </parentExtraction>
      </query>
      <result resultType={extractionEntry.resultType}>
        <fileLocation>
          {extractionEntry.destPath}
        </fileLocation>
      </result>
    </fetch>
  }

  def generateQueryXmlMultiFile(extractionEntry: ExtractionEntry, destPaths: List[String], url: String) = {
    <fetch name={extractionEntry.name}>
      <query date={new DateTime().toString(dateFormat)} type={extractionEntry.queryType}>
        <url>
          {url}
        </url>
        <path>
          {extractionEntry.path}
        </path>
        <parentExtraction>
          {extractionEntry.parentExtraction}
        </parentExtraction>
      </query>
      <result resultType={extractionEntry.resultType}>
        <fileLocations>
          {destPaths.map(e => <fileLocation>
          {e}
        </fileLocation>)}
        </fileLocations>
      </result>
    </fetch>

  }

  def generateQueryXmlSingleValue(extractionEntry: ExtractionEntry, value: String, url: String) = {
    <fetch name={extractionEntry.name}>
      <query date={new DateTime().toString(dateFormat)} type={extractionEntry.queryType}>
        <url>
          {url}
        </url>
        <path>
          {extractionEntry.path}
        </path>
        <parentExtraction>
          {extractionEntry.parentExtraction}
        </parentExtraction>
      </query>
      <result resultType={extractionEntry.resultType}>
        <value>
          {value}
        </value>
      </result>
    </fetch>
  }

  def generateQueryXmlMultiValue(extractionEntry: ExtractionEntry, values: List[String], url: String) = {
    <fetch name={extractionEntry.name}>
      <query date={new DateTime().toString(dateFormat)} type={extractionEntry.queryType}>
        <url>
          {url}
        </url>
        <path>
          {extractionEntry.path}
        </path>
        <parentExtraction>
          {extractionEntry.parentExtraction}
        </parentExtraction>
      </query>
      <result resultType={extractionEntry.resultType}>
        <values>
          {values.map(v => <value>
          {v}
        </value>)}
        </values>
      </result>
    </fetch>
  }


}

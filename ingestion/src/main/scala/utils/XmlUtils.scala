package utils

import java.io.{StringReader, StringWriter}

import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.{OutputKeys, TransformerException, TransformerFactory}
import javax.xml.xpath.{XPathConstants, XPathFactory}
import org.joda.time.DateTime
import org.w3c.dom.{Node, NodeList}
import org.xml.sax.InputSource
import sources.ExtractionEntry
import utils.Utils.{dateFormat, writeFile}

import scala.xml.{Elem, XML}

object XmlUtils {


  def processFile(doc: String,
                  extractionEntry: ExtractionEntry,
                  productId: String,
                  filename: String,
                  url: String) = {
    val destPath = extractionEntry.destPath
      .replace("(productId)", productId)
      .replace("(filename)", filename)

    val node = xPathQuery(doc, extractionEntry.query, XPathConstants.NODE).asInstanceOf[Node]

    val updatedExtrEntry = extractionEntry.copy(destPath = destPath)
    val queryXML = generateQueryXmlFile(updatedExtrEntry, url)

    writeFile(destPath, nodeToString(node))
    XML.save(s"$destPath-query", queryXML)
  }

  def generateQueryXmlFile(extractionEntry: ExtractionEntry, url: String) = {
    val fileLocation =
      <fileLocation>
        {extractionEntry.destPath}
      </fileLocation>

    generateQueryXML(extractionEntry, url, fileLocation)

  }

  def generateQueryXML(extractionEntry: ExtractionEntry, url: String, e: Elem) = {
    <fetch name={extractionEntry.name}>
      <query date={new DateTime().toString(dateFormat)} type={extractionEntry.queryType}>
        <url>
          {url}
        </url>
        <path>
          {extractionEntry.query}
        </path>
        <parentExtraction>
          {extractionEntry.parentExtraction}
        </parentExtraction>
      </query>
      <result resultType={extractionEntry.resultType}>
        {e}
      </result>
    </fetch>
  }

  def xPathQuery(docStr: String, query: String, qName: QName) = {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val doc = builder.parse(new InputSource(new StringReader(docStr)))
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

  def processMultiFile(doc: String, extractionEntry: ExtractionEntry, productId: String, url: String) = {
    val destPath = extractionEntry.destPath
      .replace("(productId)", productId)

    val nodeList = xPathQuery(doc, extractionEntry.query, XPathConstants.NODESET).asInstanceOf[NodeList]

    var destPaths = List[String]()
    for (i <- 0 until nodeList.getLength) {
      val node = nodeList.item(i)
      val destPathI = s"$destPath-($i)"
      destPaths ::= destPathI
      writeFile(destPathI, nodeToString(node))
    }

    val queryXml = generateQueryXmlMultiFile(extractionEntry, destPaths, url)

    XML.save(s"$destPath-query", queryXml)

  }

  def generateQueryXmlMultiFile(extractionEntry: ExtractionEntry, destPaths: List[String], url: String) = {
    val fileLocations =
      <fileLocations>
        {destPaths.map(e => <fileLocation>
        {e}
      </fileLocation>)}
      </fileLocations>

    generateQueryXML(extractionEntry, url, fileLocations)

  }

  def processSingleValue(doc: String, extractionEntry: ExtractionEntry, productId: String, url: String) = {
    val queryFilePath = extractionEntry.destPath.replace("(productId)", productId)

    val value = xPathQuery(doc, extractionEntry.query, XPathConstants.STRING).asInstanceOf[String]

    val queryXML = generateQueryXmlSingleValue(extractionEntry, value, url)

    XML.save(s"$queryFilePath-query", queryXML)
  }

  def generateQueryXmlSingleValue(extractionEntry: ExtractionEntry, value: String, url: String) = {
    val singleValue = <value>
      {value}
    </value>

    generateQueryXML(extractionEntry, url, singleValue)
  }

  def processMultiValue(doc: String, extractionEntry: ExtractionEntry, productId: String, url: String) = {
    val destPath = extractionEntry.destPath.replace("(productId)", productId)

    val nodeList = xPathQuery(doc, extractionEntry.query, XPathConstants.NODESET).asInstanceOf[NodeList]

    var values = List[String]()
    for (i <- 0 until nodeList.getLength) {
      val node = nodeList.item(i)
      values ::= xPathQuery(node, "/", XPathConstants.STRING).asInstanceOf[String]
    }

    val queryXml = generateQueryXmlMultiValue(extractionEntry, values, url)
    XML.save(s"$destPath-query", queryXml)
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

  def generateQueryXmlMultiValue(extractionEntry: ExtractionEntry, values: List[String], url: String) = {

    val multiValue = <values>
      {values.map(v => <value>
        {v}
      </value>)}
    </values>

    generateQueryXML(extractionEntry, url, multiValue)

  }


}

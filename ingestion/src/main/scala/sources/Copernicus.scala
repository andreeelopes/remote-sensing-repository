package sources


import com.typesafe.config.Config
import commons.Work
import utils.Utils._

class CopernicusSource(config: Config) extends Source("copernicusOAH", config) {

  //  val epochDate = new DateTime().minus(epoch)

  def generateQuery() = {
    url = s"${baseUrl}start=$pageStart&rows=$pageSize&" +
      s"q=ingestiondate:[${ingestionStart.toString(dateFormat)} TO ${ingestionEnd.toString(dateFormat)}]"

    url = url.replaceAll(" ", "%20")
  }

  override def generateWork() = {
    generateQuery()
    advanceIngestionWindow()

    println("URL: " + url)

    new CopernicusWork(this)
  }


}

class CopernicusWork(source: CopernicusSource) extends Work(source) {

  override def preProcess(metadata: String) = {

    metadata

    //    val links = xmlElem.child.filter(node => node.label.equals("link"))
    //    val link = links.filter(node => (node \@ "rel").equals("next")).head
    //    val next = link \@ "href"

  }

}
package sources

import com.typesafe.config.Config
import commons.Work

class CopernicusSource(config: Config) extends Source("copernicusOAH", config) {
  override def generateWork() = new CopernicusWork(this)
}

class CopernicusWork(source: CopernicusSource) extends Work(source) {

  override def preProcess(metadata: String) = {
    metadata

    //    val links = xmlElem.child.filter(node => node.label.equals("link"))
    //    val link = links.filter(node => (node \@ "rel").equals("next")).head
    //    val next = link \@ "href"

  }

}
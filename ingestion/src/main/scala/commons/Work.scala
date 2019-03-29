package commons

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import sources.Source
import utils.Utils

case class WorkResult(workId: String, result: Any)

abstract class Work(src: Source) {

  val source = src

  def execute(implicit context: ActorContext, actorMat: ActorMaterializer)

  def workId = Utils.generateWorkId()

}


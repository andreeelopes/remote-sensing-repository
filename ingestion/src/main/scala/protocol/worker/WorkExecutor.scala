package protocol.worker

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer
import sources.Work
import utils.KryoSerializable


/**
  *
  * commons.Work executor is the actor actually performing the work.
  */
object WorkExecutor {
  def props = Props(new WorkExecutor)

  case class DoWork(work: Work) extends KryoSerializable

  case class WorkComplete(nextWork: List[Work]) extends KryoSerializable

}

class WorkExecutor extends Actor with ActorLogging {

  import WorkExecutor._

  implicit val materializer = ActorMaterializer()


  def receive = {
    case DoWork(work) => work.execute
  }


}

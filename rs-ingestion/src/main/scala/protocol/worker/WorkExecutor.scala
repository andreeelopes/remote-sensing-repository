package protocol.worker

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.ActorMaterializer
import sources.Work


/**
  *
  * commons.Work executor is the actor actually performing the work.
  */
object WorkExecutor {
  def props = Props(new WorkExecutor)

  case class DoWork(work: Work)

  case class WorkComplete(nextWork: List[Work])

}

class WorkExecutor extends Actor with ActorLogging {

  import WorkExecutor._

  implicit val materializer: ActorMaterializer = ActorMaterializer()


  def receive: Receive = {
    case DoWork(work) => work.execute

    case e: Exception => throw e
  }


}


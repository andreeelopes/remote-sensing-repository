package worker


import akka.actor.{Actor, ActorLogging, Props}
import commons.Work
import akka.stream.ActorMaterializer


/**
  *
  * commons.Work executor is the actor actually performing the work.
  */
object WorkExecutor {
  def props = Props(new WorkExecutor)

  case class DoWork(work: Work)

  case class WorkComplete(result: String)

}

class WorkExecutor extends Actor with ActorLogging {

  import WorkExecutor._

  implicit val materializer = ActorMaterializer()


  def receive = {
    case DoWork(work) => work.execute
  }


}


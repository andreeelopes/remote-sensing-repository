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

  implicit val materializer = ActorMaterializer()


  def receive = {
    case DoWork(work) =>
      //      context.system.scheduler.scheduleOnce(work.source.workTimeout, self, new Exception(s"Work ${work.workId} timed out"))
      work.execute

    case e: Exception => throw new Exception(e)
  }


}


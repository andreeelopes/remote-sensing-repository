package protocol.worker

import java.util.UUID

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import protocol.master.MasterWorkerProtocol._
import sources.Work

import scala.concurrent.duration._

/**
  * The worker is actually more of a middle manager, delegating the actual work
  * to the WorkExecutor, supervising it and keeping itself available to interact with the work master.
  */
object Worker {

  def props(masterProxy: ActorRef): Props = Props(new Worker(masterProxy))

}

class Worker(masterProxy: ActorRef)
  extends Actor with ActorLogging {

  import context.dispatcher


  private val workerId = UUID.randomUUID().toString
  private val registerInterval = context.system.settings.config.getDuration("distributed-workers.worker-registration-interval").getSeconds.seconds

  private val registerTask = context.system.scheduler.schedule(0.seconds, registerInterval, masterProxy, RegisterWorker(workerId))

  private val workExecutor = createWorkExecutor()

  var currentWorkId: Option[String] = None

  def workId: String = currentWorkId match {
    case Some(workId) => workId
    case None => throw new IllegalStateException("Not working")
  }

  def receive: Receive = idle

  def idle: Receive = {
    case WorkIsReady =>
      // this is the only state where we reply to WorkIsReady
      masterProxy ! WorkerRequestsWork(workerId)

    case work: Work =>
      log.info("Got work: {}", work.workId)
      currentWorkId = Some(work.workId)
      workExecutor ! WorkExecutor.DoWork(work)
      context.become(working)

  }

  def working: Receive = {
    case WorkExecutor.WorkComplete(nextWork) =>
      log.info("Work is complete. Exists more work? {}.", nextWork)
      masterProxy ! WorkIsDone(workerId, workId, nextWork)
      context.setReceiveTimeout(5.seconds)
      context.become(waitForWorkIsDoneAck(nextWork))

    case _: Work =>
      log.warning("Yikes. Master told me to do work, while I'm already working.")
  }

  def waitForWorkIsDoneAck(nextWork: List[Work]): Receive = {
    case Ack(id) if id == workId =>
      masterProxy ! WorkerRequestsWork(workerId)
      context.setReceiveTimeout(Duration.Undefined)
      context.become(idle)

    case ReceiveTimeout =>
      log.info("No ack from master, resending work result")
      masterProxy ! WorkIsDone(workerId, workId, nextWork)
  }

  def createWorkExecutor(): ActorRef =
  // in addition to starting the actor we also watch it, so that
  // if it stops this worker will also be stopped
    context.watch(context.actorOf(WorkExecutor.props, s"work-executor-${self.path.name}"))

  override def supervisorStrategy: OneForOneStrategy = OneForOneStrategy() {
    case _: ActorInitializationException => Stop
    case _: Exception =>
      currentWorkId foreach { workId =>
        masterProxy ! WorkFailed(workerId, workId)
      }
      context.become(idle)
      Restart
  }

  override def postStop(): Unit = {
    registerTask.cancel()
    masterProxy ! DeRegisterWorker(workerId)
  }

}
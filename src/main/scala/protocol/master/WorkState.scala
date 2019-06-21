package protocol.master


import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import sources.Work

object WorkState {

  val workMaxTries: Int = ConfigFactory.load().getInt("distributed-workers.max-work-tries")
  val workTimeoutMultiplier: Int = ConfigFactory.load().getInt("distributed-workers.work-timeout-multiplier")
  val workBackoffBase: Double = ConfigFactory.load().getDouble("distributed-workers.work-backoff-base")

  def empty: WorkState = WorkState(
    pendingWork = List.empty,
    workInProgress = Map.empty,
    workInProgressPerSource = Master.sourceLimits.mapValues(_ => 0),
    acceptedWorkIds = Set.empty,
    doneWorkIds = Set.empty,
    failedWorkIds = Set.empty)

  trait WorkDomainEvent

  // #events
  case class WorkAccepted(work: Work) extends WorkDomainEvent

  case class WorkStarted(workId: String) extends WorkDomainEvent

  case class WorkCompleted(workId: String, result: Any) extends WorkDomainEvent

  case class WorkerFailed(workId: String) extends WorkDomainEvent

  case class WorkerTimedOut(workId: String) extends WorkDomainEvent

}

case class WorkState(
                      val pendingWork: List[Work],
                      val workInProgress: Map[String, Work],
                      val workInProgressPerSource: Map[String, Int],
                      val acceptedWorkIds: Set[String],
                      val doneWorkIds: Set[String],
                      val failedWorkIds: Set[String]
                    ) {

  import WorkState._

  val isValidWork: Work => Boolean =
    (w: Work) =>
      workInProgressPerSource(w.source.configName) < Master.sourceLimits(w.source.configName) &&
        w.nTries <= workMaxTries &&
        w.backOffTimeout.isBefore(new DateTime())

  def hasWork: Boolean = pendingWork.exists(isValidWork(_))

  def nextWork: Work = pendingWork.filter(isValidWork(_)).head

  def isAccepted(workId: String): Boolean = acceptedWorkIds.contains(workId)

  def isInProgress(workId: String): Boolean = workInProgress.contains(workId)

  def isDone(workId: String): Boolean = doneWorkIds.contains(workId)

  def updated(event: WorkDomainEvent): WorkState =
    event match {
      case WorkAccepted(work) =>

        copy(
          pendingWork = pendingWork ::: List(work),
          acceptedWorkIds = acceptedWorkIds + work.workId
        )
      case WorkStarted(workId) =>

        val work = pendingWork.filter(w => w.workId == workId).head
        val failedWork = pendingWork.filter(w => w.nTries > workMaxTries).map(_.workId)
        val rest = pendingWork.filterNot(w => w.workId == workId || w.nTries > workMaxTries)

        require(workId == work.workId, s"WorkStarted expected workId $workId == ${work.workId}")
        copy(
          pendingWork = rest,
          workInProgress = workInProgress + (workId -> work),
          workInProgressPerSource = updatedConcurrencySource(work),
          failedWorkIds = failedWorkIds ++ failedWork
        )
      case WorkCompleted(workId, _) =>

        val work = workInProgress(workId)

        copy(
          workInProgress = workInProgress - workId,
          doneWorkIds = doneWorkIds + workId,
          workInProgressPerSource = updatedConcurrencySource(work, increment = false)
        )
      case WorkerFailed(workId) => handleFailure(workId)
      case WorkerTimedOut(workId) => handleFailure(workId)


    }


  private def handleFailure(workId: String): WorkState = {
    val workOpt = workInProgress.get(workId)

    if (workOpt.isDefined) {
      val updatedWork = updateWork(workOpt.get)

      copy(
        pendingWork = pendingWork ::: List(updatedWork),
        workInProgress = workInProgress - workId,
        workInProgressPerSource = updatedConcurrencySource(updatedWork, increment = false)
      )
    } else this //conflict between previous check (busy or idle) and workInProgress(workId)

  }

  private def updatedConcurrencySource(work: Work, increment: Boolean = true): Map[String, Int] = {

    var updatedWorkInProgressPerSource = workInProgressPerSource
    val sourceConcurrency = workInProgressPerSource(work.source.configName)
    val updatedSourceConcurrency = if (increment) sourceConcurrency + 1 else sourceConcurrency - 1

    updatedWorkInProgressPerSource += (work.source.configName -> updatedSourceConcurrency)

    updatedWorkInProgressPerSource
  }

  private def updateWork(work: Work): Work = {
    work.nTries += 1
    work.workTimeout *= workTimeoutMultiplier
    work.backOffInterval = math.pow(workBackoffBase, work.backOffInterval)
    work.backOffTimeout = new DateTime().plusSeconds(work.backOffInterval.toInt)
    work
  }


}

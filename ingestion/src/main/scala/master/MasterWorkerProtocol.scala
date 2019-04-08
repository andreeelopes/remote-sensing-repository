package master

import commons.{KryoSerializable, Work}

object MasterWorkerProtocol {
  // Messages from Workers
  case class RegisterWorker(workerId: String)
  case class DeRegisterWorker(workerId: String)
  case class WorkerRequestsWork(workerId: String)
  case class WorkIsDone(workerId: String, workId: String, nextWork: List[Work]) extends KryoSerializable
  case class WorkFailed(workerId: String, workId: String)

  // Messages to Workers
  case object WorkIsReady
  case class Ack(id: String)
}

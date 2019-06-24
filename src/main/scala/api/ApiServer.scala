package api

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import protocol.scheduler.Orchestrator.ProduceWork
import sources.Work
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


////#user-case-classes
//final case class User(name: String, age: Int, countryOfResidence: String)
//final case class Users(users: Seq[User])
////#user-case-classes
//
//object UserRegistryActor {
//  final case class ActionPerformed(description: String)
//  final case object GetUsers
//  final case class CreateUser(user: User)
//  final case class GetUser(name: String)
//  final case class DeleteUser(name: String)
//
//  def props: Props = Props[UserRegistryActor]
//}

object ApiServer {
  val requestsTopic = "requests"

  def props: Props = Props(new ApiServer)

}

class ApiServer extends Actor with ActorLogging {

  import ApiServer._

  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  def receive: Receive = {
    case "hello" => sender() ! "hello-back"
  }

  private def produceWork(work: Work): Unit = {
    mediator ! DistributedPubSubMediator.Publish(requestsTopic, ProduceWork(work))
  }

}

package api

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import sources.{FetchAndSaveSource, FetchAndSaveWork, Work}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import mongo.MongoDAO
import org.mongodb.scala.bson.{BsonDocument, BsonString, BsonValue}

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object Services {
  val requestsTopic = "requests"
  val resultsTopic = "results"

  def props: Props = Props(new Services)

  final case class ActionPerformed(statusCode: Int, description: String)

  final case class FetchData(productId: String, dataObjectId: String)

  final case class FetchDataWork(work: Work)

}

class Services extends Actor with ActorLogging {

  import Services._

  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  var router: ActorRef = _

  val timeoutConf: FiniteDuration = ConfigFactory.load().getDuration("api.ask-timeout").getSeconds.seconds
  implicit lazy val timeout: Timeout = Timeout(timeoutConf)

  def receive: Receive = {
    case FetchData(productId, dataObjectId) =>
      router = sender() // always the same

      val docFut = MongoDAO.getDoc(productId)
      val doc = Await.result(docFut, 5000 millis)

      if (doc != null) {

        val program = doc.getString("program")
        val tierOpt = doc.getString("collectionTier") //sentinels do not have this field
        val dataObjectDocOpt = doc.get(s"data.$dataObjectId")

        if (dataObjectDocOpt.isEmpty)
          router ! ActionPerformed(StatusCodes.BadRequest.intValue, "Requested data is not available")
        else if (tierOpt != "T1" || tierOpt != "RT" || tierOpt != null)
          router ! ActionPerformed(StatusCodes.BadRequest.intValue, "Landsat data only available for T1 or RT tier")
        else {
          val configName = if (program == "landsat") "amazon" else "copernicus-oah-odata"

          val dataObjectDoc = dataObjectDocOpt.get.asDocument()
          val status = dataObjectDoc.get("status").asString()

          if (status.toString == "remote") {
            val url = dataObjectDoc.get("url").asString().toString

            val work: Work = new FetchAndSaveWork(new FetchAndSaveSource(configName), productId, dataObjectId, url)

            // send To Orchestrator
            mediator ! DistributedPubSubMediator.Publish(requestsTopic, FetchDataWork(work))
          }
          else {
            router ! ActionPerformed(StatusCodes.BadRequest.intValue, "Data is already stored locally")
          }
        }

      } else {
        router ! ActionPerformed(StatusCodes.NotFound.intValue, "Product does not exist")
      }


    case a: ActionPerformed => router ! a
  }


}

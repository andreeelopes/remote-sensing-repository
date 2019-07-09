package api

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.{DistributedPubSub, DistributedPubSubMediator}
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import sources.{FetchAndSaveSource, FetchAndSaveWork, Work}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import mongo.MongoDAO
import org.mongodb.scala.bson.{BsonDocument, BsonString, BsonValue}
import play.api.libs.json.{JsArray, JsObject, JsValue, Json, __}
import sources.handlers.AuthConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.Try

object Services {
  val requestsTopic = "requests"
  val resultsTopic = "results"

  val config: Config = ConfigFactory.load()

  def props: Props = Props(new Services)

  final case class ActionPerformed(statusCode: Int, description: String)

  final case class ActionPerformedOrchestrator(statusCode: Int, description: String)

  final case class FetchData(productId: String, dataObjectId: String)

  final case class FetchDataWork(work: Work)

}

class Services extends Actor with ActorLogging {

  import Services._

  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  var router: ActorRef = _

  val timeoutConf: FiniteDuration = ConfigFactory.load().getDuration("api.ask-timeout").getSeconds.seconds
  implicit lazy val timeout: Timeout = Timeout(timeoutConf)

  var productId: String = _
  var dataObjectId: String = _
  var provider: String = _

  def receive: Receive = {
    case fd: FetchData =>
      router = sender() // always the same

      productId = fd.productId
      dataObjectId = fd.dataObjectId

      val docOpt = MongoDAO.getDoc(productId)

      if (docOpt.isDefined) {
        val doc = docOpt.get
        val json = Json.parse(doc.toJson())

        val data = (json \ "data").as[Map[String, JsValue]]
        provider = (json \ "provider").as[String]
        val productType = (json \ "productType").as[String]

        val nonImageryDataOpt =
          Try {
            data.filter { kv =>
              val idOpt = kv._2 \ "_id"
              idOpt.isDefined && idOpt.get.as[String] == dataObjectId
            }.head._2
          }.toOption

        val imageryDataOpt =
          Try {
            data("imagery").as[JsArray].value
              .filter { dataObject =>
                val idOpt = dataObject \ "_id"
                idOpt.isDefined && idOpt.get.as[String] == dataObjectId
              }.head
          }.toOption


        if (nonImageryDataOpt.isEmpty && imageryDataOpt.isEmpty)
          router ! ActionPerformed(StatusCodes.NotFound.intValue, "Requested object does not exist")
        else {
          val dataObject = if (imageryDataOpt.isDefined) imageryDataOpt.get else nonImageryDataOpt.get
          val status = (dataObject \ "status").as[String]

          if (status == "pending")
            router ! ActionPerformed(StatusCodes.BadRequest.intValue, "Data download is already scheduled")
          else if (status == "local")
            router ! ActionPerformed(StatusCodes.BadRequest.intValue, "Data is already stored locally")
          else {
            val url = (dataObject \ "url").as[String]
            val fileName = (dataObject \ "fileName").as[String]
            val size = Try((dataObject \ "size").as[Long]).toOption.getOrElse[Long](111111111)

            val workTimeout = (size / 1000000) + 20 seconds // ~1MB/s
            val configName = if (provider == "copernicus") "copernicus-oah-data" else "earth-explorer-download-api"
            val sourceName = if (provider == "copernicus") "copernicus-oah-data" else "earth-explorer"

            val work: Work = new FetchAndSaveWork(
              new FetchAndSaveSource(configName, provider == "earth-explorer", productType, Some(AuthConfig(sourceName, config))),
              productId, dataObjectId, url, workTimeout, fileName
            )

            // send To Orchestrator
            mediator ! DistributedPubSubMediator.Publish(requestsTopic, FetchDataWork(work))
          }
        }
      }
      else
        router ! ActionPerformed(StatusCodes.NotFound.intValue, "Product does not exist")


    case a: ActionPerformed =>
      MongoDAO.updateProductData(productId, dataObjectId, Json.obj("status" -> "pending"), provider == "earth-explorer")
      router ! a
  }


}

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

  def receive: Receive = {
    case fd: FetchData =>
      productId = fd.productId
      dataObjectId = fd.dataObjectId
      router = sender() // always the same

      val docFut = MongoDAO.getDoc(productId)
      val doc = Await.result(docFut, 5000 millis)

      if (doc != null) {
        val json = Json.parse(doc.toJson())

        val data = (json \ "data").as[Map[String, JsValue]]

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
          router ! ActionPerformed(StatusCodes.NotFound.intValue, "Requested data is not available")
        else {

          val dataObject = if (imageryDataOpt.isDefined) imageryDataOpt.get else nonImageryDataOpt.get
          val status = (dataObject \ "status").as[String]
          val url = (dataObject \ "url").as[String]
          val fileName = (dataObject \ "fileName").as[String]
          val size = Try((dataObject \ "size").as[Long]).toOption.getOrElse[Long](111111111)

          if (status == "local")
            router ! ActionPerformed(StatusCodes.BadRequest.intValue, "Data download is already scheduled")
          else if (status == "pending")
            router ! ActionPerformed(StatusCodes.BadRequest.intValue, "Data is already stored locally")
          else {
            val work: Work = new FetchAndSaveWork(new FetchAndSaveSource("copernicus-oah-odata"),
              productId, dataObjectId, url, size, fileName)

            // send To Orchestrator
            mediator ! DistributedPubSubMediator.Publish(requestsTopic, FetchDataWork(work))
          }

        }

      } else {
        router ! ActionPerformed(StatusCodes.NotFound.intValue, "Product does not exist")
      }


    case a: ActionPerformed =>

      MongoDAO.getDoc(productId).onComplete { doc =>
        val docJson = Json.parse(doc.get.toJson)

        var indicator = true

        val imageryTransformer = __.read[JsArray].map {
          case JsArray(values) =>
            JsArray(values.map { e =>
              val idOpt = (e \ "_id").asOpt[String]
              if (idOpt.isDefined && idOpt.get == dataObjectId) {
                (e.as[JsObject] ++ Json.obj("status" -> "pending")).as[JsValue]
              } else
                e
            })
        }

        val othersTransformer = __.read[JsValue].map { data =>
          val dataMap = data.as[Map[String, JsValue]]

          JsObject(dataMap.map { kv =>
            val idOpt = (kv._2 \ "_id").asOpt[String]
            if (idOpt.isDefined && idOpt.get == dataObjectId) {
              indicator = false
              val jsValue = (kv._2.as[JsObject] ++ Json.obj("status" -> "pending")).as[JsValue]
              (kv._1, jsValue)
            } else kv
          }).as[JsValue]
        }

        // update the "values" field in the original json
        val jsonImageryTransformer = (__ \ 'imagery).json.update(imageryTransformer)
        val jsonOthersTransformer = __.json.update(othersTransformer)

        val data = (docJson \ "data").as[JsValue]
        // carry out the transformation
        val transformedImageryJson = data.transform(jsonImageryTransformer).asOpt
        val transformedOthersJson = data.transform(jsonOthersTransformer).asOpt

        val updatedDoc = if (indicator)
          transformedImageryJson.get.as[JsValue]
        else
          transformedOthersJson.get.as[JsValue]

        MongoDAO.addFieldToDoc(productId, "data", BsonDocument(updatedDoc.toString()))
      }


      router ! a
  }


}

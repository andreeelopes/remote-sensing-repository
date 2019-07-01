package sources

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import mongo.MongoDAO
import org.bson.Document
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.BsonDocument
import play.api.libs.json._
import sources.handlers.{AuthConfig, ErrorHandlers}
import utils.HTTPClient.singleRequest
import utils.Utils.writeFile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Try


class FetchAndSaveSource(configName: String,
                         val errorHandler: (Int, Array[Byte], String, ActorMaterializer) => Unit = ErrorHandlers.defaultErrorHandler)
  extends Source(configName) {
  override val authConfigOpt: Option[AuthConfig] = Some(AuthConfig(configName, config))
}

class FetchAndSaveWork(override val source: FetchAndSaveSource,
                       productId: String,
                       dataObjectId: String,
                       url: String,
                       size: Long,
                       fileName: String) extends Work(source) {

  workTimeout = (size / 1000000) + 20 seconds // 1MB/s

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    singleRequest(url, workTimeout, process, source.errorHandler, source.authConfigOpt)
  }

  override def process(responseBytes: Array[Byte]): List[Work] = {
    val dest = s"${source.baseDir}/$productId/$fileName"

    MongoDAO.getDoc(productId).onComplete { doc =>
      val docJson = Json.parse(doc.get.toJson)

      var indicator = true

      val imageryTransformer = __.read[JsArray].map {
        case JsArray(values) =>
          JsArray(values.map { e =>
            val idOpt = (e \ "_id").asOpt[String]
            if (idOpt.isDefined && idOpt.get == dataObjectId) {
              (e.as[JsObject] ++ Json.obj("status" -> "local", "url" -> dest)).as[JsValue]
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
            val jsValue = (kv._2.as[JsObject] ++ Json.obj("status" -> "local", "url" -> dest)).as[JsValue]
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


    writeFile(dest, responseBytes)

    List()
  }

}

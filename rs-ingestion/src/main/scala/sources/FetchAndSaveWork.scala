package sources

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import mongo.MongoDAO
import org.mongodb.scala.bson.{BsonDocument, BsonString}
import sources.handlers.{AuthConfig, ErrorHandlers}
import utils.HTTPClient.singleRequest
import utils.Utils.writeFile


class FetchAndSaveSource(configName: String,
                         val errorHandler: (Int, Array[Byte], String, ActorMaterializer) => Unit = ErrorHandlers.defaultErrorHandler,
                         override val authConfigOpt: Option[AuthConfig] = None)
  extends Source(configName)

class FetchAndSaveWork(override val source: FetchAndSaveSource, productId: String, dataObjectId: String, url: String) extends Work(source) {

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    singleRequest(url, workTimeout, process, source.errorHandler, source.authConfigOpt)
  }

  override def process(responseBytes: Array[Byte]): List[Work] = {
    val filename = url.split("/").last
    val dest = s"${source.baseDir}/$productId/$filename"

    writeFile(dest, responseBytes)

    val mongoDoc = BsonDocument("status" -> BsonString("local"), "url" -> BsonString(dest))
    MongoDAO.addFieldToDoc(productId, s"data.$dataObjectId", mongoDoc)

    List()
  }

}

package sources

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import mongo.MongoDAO
import org.joda.time.DateTime
import org.json.HTTP
import org.mongodb.scala.{Completed, Document}
import org.mongodb.scala.bson.{BsonDocument, BsonInt64, BsonString, BsonValue}
import sources.handlers.ErrorHandlers
import utils.HTTPClient
import utils.HTTPClient._
import utils.Utils.{dateFormat, getAllExtractions}

abstract class ProviderPeriodicRESTSource(configName: String, config: Config,
                                          val program: String,
                                          val platform: String,
                                          val productType: String)
  extends PeriodicRESTSource(configName, config) {
  val PROVIDER: String

  val extractions: List[Extraction] = getAllExtractions(configName, program, platform, productType)
}

abstract class ProviderPeriodicRESTWork(override val source: ProviderPeriodicRESTSource,
                                        override val ingestionDates: (DateTime, DateTime),
                                        override val isEpoch: Boolean = false,
                                        override val pageStart: Int = 0)
  extends PeriodicRESTWork(source, ingestionDates, isEpoch, pageStart) {

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    singleRequest(url, workTimeout, process, ErrorHandlers.defaultErrorHandler, source.authConfigOpt)
  }

  override def saveFetchingLog(result: BsonValue): Unit = {
    val bsonDoc: BsonDocument = BsonDocument(
      "query" -> BsonDocument(
        "url" -> BsonString(url),
        "provider" -> BsonString(source.PROVIDER),
        "productType" -> BsonString(source.productType),
        "pageStart" -> BsonInt64(pageStart),
        "pageEnd" -> BsonInt64(pageStart + source.pageSize),
        "startDate" -> BsonString(ingestionDates._1.toString(dateFormat)),
        "endDate" -> BsonString(ingestionDates._2.toString(dateFormat)),
      ),
      "result" -> result,
    )
    MongoDAO.insertDoc(bsonDoc, MongoDAO.PERIODIC_FETCHING_LOG_COL)
  }

  def setupEntryMongo(productId: String): Unit = {
    MongoDAO.insertDoc(
      Document(
        "_id" -> productId,
        "program" -> source.program,
        "platform" -> source.platform,
        "productType" -> source.productType
      ),
      MongoDAO.PRODUCTS_COL)
  }


}


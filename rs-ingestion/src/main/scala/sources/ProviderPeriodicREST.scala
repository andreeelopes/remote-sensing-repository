package sources

import java.util.concurrent.TimeUnit

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import mongo.MongoDAO
import org.joda.time
import org.joda.time.DateTime
import org.json.HTTP
import org.mongodb.scala.{Completed, Document}
import org.mongodb.scala.bson.{BsonDateTime, BsonDocument, BsonInt64, BsonString, BsonValue}
import protocol.scheduler.Orchestrator.ProduceWork
import sources.handlers.ErrorHandlers
import utils.HTTPClient._
import utils.Utils
import utils.Utils._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}

abstract class ProviderPeriodicSource(configName: String, config: Config,
                                      val program: String,
                                      val platform: String,
                                      val productType: String)
  extends PeriodicRESTSource(configName, config) {
  val PROVIDER: String

  val extractions: List[Extraction] = getAllExtractions(configName, program, platform, productType)

  val productConf: ProductConf = Utils.getProductConf(configName, program, platform, productType)
  override val fetchingFrequency: FiniteDuration = productConf.fetchingFrequency
  override val epoch: Int = productConf.epoch

  val startDelay: FiniteDuration = config.getDuration(s"orchestrator.start-delay").getSeconds.seconds

  val historyEnd: DateTime = new DateTime()
  val historyDates: (DateTime, DateTime) = (historyEnd.minusYears(epoch), historyEnd)
  val initial: (DateTime, DateTime) = (historyEnd, historyEnd.plus(fetchingFrequency.toMillis))
  val epochWork: ProviderPeriodicWork
  val periodicInitialWork: ProviderPeriodicWork

  def adjustWindow(intervalDates: (DateTime, DateTime)): (DateTime, DateTime) =
    (intervalDates._2, intervalDates._2.plus(fetchingFrequency.toMillis))


  override def start(implicit context: ActorContext): Unit = {

    val doc = MongoDAO.getLastPeriodicLogByProductType(productType)

    if (doc.isEmpty) {
      context.system.scheduler.scheduleOnce(startDelay, context.self, ProduceWork(epochWork))
      //      context.system.scheduler.scheduleOnce(startDelay, context.self, ProduceWork(periodicInitialWork)) TODO
    } else {
      val endDate = new DateTime(doc.get.getDocument("query").getDateTime("endDate").getValue)

      val periodicWork = generateWork((endDate, new DateTime()))
      context.system.scheduler.scheduleOnce(startDelay, context.self, ProduceWork(periodicWork))
    }
  }

}

abstract class ProviderPeriodicWork(override val source: ProviderPeriodicSource,
                                    override val intervalDates: (DateTime, DateTime),
                                    override val isEpoch: Boolean = false,
                                    override val pageStart: Int = 0)
  extends PeriodicRESTWork(source, intervalDates, isEpoch, pageStart) {

  override def execute()(implicit context: ActorContext, mat: ActorMaterializer): Unit = {
    singleRequest(url, workTimeout, process, ErrorHandlers.defaultErrorHandler, source.authConfigOpt)
  }

  def saveFetchingLog(result: BsonValue): Unit = {
    val bsonDoc: BsonDocument = BsonDocument(
      "_id" -> BsonString(s"${source.productType}-${intervalDates._1}-${intervalDates._2}-$pageStart"),
      "query" -> BsonDocument(
        "url" -> BsonString(url),
        "provider" -> BsonString(source.PROVIDER),
        "productType" -> BsonString(source.productType),
        "pageStart" -> BsonInt64(pageStart),
        "pageEnd" -> BsonInt64(pageStart + source.pageSize),
        "startDate" -> BsonDateTime(intervalDates._1.toDate),
        "endDate" -> BsonDateTime(intervalDates._2.toDate),
      ),
      "result" -> result,
    )
    MongoDAO.insertDoc(bsonDoc, MongoDAO.PERIODIC_FETCHING_LOG_COL)
  }

  def setupEntryMongo(productId: String): Unit = {
    MongoDAO.insertDoc(
      BsonDocument(
        "_id" -> BsonString(productId),
        "program" -> BsonString(source.program),
        "platform" -> BsonString(source.platform),
        "productType" -> BsonString(source.productType),
        "provider" -> BsonString(source.PROVIDER),
        "ingestionDate" -> BsonDateTime(new DateTime().toDate),
        "custom" -> BsonDocument()
      )
    )
  }


}




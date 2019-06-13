package sources

import com.typesafe.config.Config
import mongo.MongoDAO
import org.joda.time.DateTime
import org.mongodb.scala.Completed
import org.mongodb.scala.bson.{BsonDocument, BsonInt64, BsonString, BsonValue}
import play.api.libs.json.JsValue
import utils.Utils.dateFormat


abstract class PeriodicRESTSource(configName: String, config: Config) extends PeriodicSource(configName, config) {
  val baseUrl: String = config.getString(s"sources.$configName.base-url")
  val pageSize: Int = config.getInt(s"sources.$configName.page-size")
}

abstract class PeriodicRESTWork(override val source: PeriodicRESTSource,
                                override val ingestionDates: (DateTime, DateTime),
                                override val isEpoch: Boolean = false,
                                val pageStart: Int = 0) extends PeriodicWork(source, ingestionDates, isEpoch) {

  val url: String

  def getNextPagesWork(doc: JsValue): Option[Work]

  def saveFetchingLog(result: BsonValue): Unit = {
    val bsonDoc: BsonDocument = BsonDocument(
      "query" -> BsonDocument(
        "url" -> BsonString(url),
        "pageStart" -> BsonInt64(pageStart),
        "pageEnd" -> BsonInt64(pageStart + source.pageSize),
        "startDate" -> BsonString(ingestionDates._1.toString(dateFormat)),
        "endDate" -> BsonString(ingestionDates._2.toString(dateFormat)),
      ),
      "result" -> result,
    )
    MongoDAO.insertDoc(bsonDoc, MongoDAO.PERIODIC_FETCHING_LOG_COL)
  }

}

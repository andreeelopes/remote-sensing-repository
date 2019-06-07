package mongo

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import mongo.Helpers._
import org.mongodb.scala.bson.BsonValue
import org.mongodb.scala.result.UpdateResult
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

object MongoDAO {

  val config: Config = ConfigFactory.load()

  var uri = "mongodb://"
  config.getConfigList("mongo.ip-port").asScala.toList.foreach { entry =>
    uri += s"${entry.getString("ip")}:${entry.getInt("port")},"
  }

  val mongoClient: MongoClient = MongoClient(uri)
  Thread.sleep(1000)

  val DB_NAME = "rsDB"

  val COMMON_COL = "commonMD"

  val FETCHING_LOG_COL = "fetchingLog"
  val PERIODIC_FETCHING_LOG_COL = "periodicFetchingLog"

  val database: MongoDatabase = mongoClient.getDatabase(DB_NAME)

  private var collections = Map(
    COMMON_COL -> database.getCollection(COMMON_COL),
    FETCHING_LOG_COL -> database.getCollection(FETCHING_LOG_COL),
    PERIODIC_FETCHING_LOG_COL -> database.getCollection(PERIODIC_FETCHING_LOG_COL)
  )

  collections(COMMON_COL).drop().results()
  collections(FETCHING_LOG_COL).drop().results()
  collections(PERIODIC_FETCHING_LOG_COL).drop().results()


  def insertDoc(doc: Document, collectionName: String): Seq[Completed] = {
    getOrCreateCollection(collectionName).insertOne(doc).results()
  }

  def addFieldToDoc(docId: String, field: String, value: BsonValue, collectionName: String): Seq[UpdateResult] = {
    getOrCreateCollection(collectionName).updateOne(equal("_id", docId), set(field, value)).results()
  }

  private def getOrCreateCollection(collectionName: String, drop: Boolean = true) = {
    collections.getOrElse(collectionName, {
      val col = database.getCollection(collectionName)
      if (drop) col.drop.results()
      collections += (collectionName -> col)
      col
    })

  }

}

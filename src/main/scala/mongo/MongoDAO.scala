package mongo

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import mongo.Helpers._
import org.mongodb.scala.bson.{BsonDocument, BsonString, BsonValue}
import com.typesafe.config.{Config, ConfigFactory}
import org.mongodb.scala.model.Projections._
import sources.Extraction

import scala.collection.JavaConverters._
import scala.concurrent.Future

object MongoDAO {

  val config: Config = ConfigFactory.load()

  var uri = "mongodb://"
  config.getConfigList("mongo.ip-port").asScala.toList.foreach { entry =>
    uri += s"${entry.getString("ip")}:${entry.getInt("port")},"
  }

  val mongoClient: MongoClient = MongoClient(uri)
  Thread.sleep(1000)

  val DB_NAME = "rsDB"

  val PRODUCTS_COL = "productsMD"

  val FETCHING_LOG_COL = "fetchingLog"
  val PERIODIC_FETCHING_LOG_COL = "periodicFetchingLog"
  val EARTH_EXPLORER_TOKENS = "eeTokens"

  val database: MongoDatabase = mongoClient.getDatabase(DB_NAME)

  database.drop().results() // TODO remove

  private var collections = Map(
    PRODUCTS_COL -> database.getCollection(PRODUCTS_COL),
    FETCHING_LOG_COL -> database.getCollection(FETCHING_LOG_COL),
    PERIODIC_FETCHING_LOG_COL -> database.getCollection(PERIODIC_FETCHING_LOG_COL),
    EARTH_EXPLORER_TOKENS -> database.getCollection(EARTH_EXPLORER_TOKENS),
  )

  insertDoc(BsonDocument("_id" -> "token", "token" -> "NA"), EARTH_EXPLORER_TOKENS)

  def insertDoc(doc: Document, collectionName: String): Unit = {
    try {
      getOrCreateCollection(collectionName).insertOne(doc).results()
    }
    catch {
      case e: MongoException => println(e.getMessage)
    }
  }

  def updateUrl(extraction: Extraction, productId: String): Unit = {

    getOrCreateCollection(PRODUCTS_COL)
      .updateOne(
        equal("_id", productId),
        set(s"data.${extraction.name}",
          BsonDocument("status" -> BsonString("local"), "url" -> BsonString(extraction.destPath)))
      )
      .results()
  }

  def updateToken(docId: String, value: BsonValue): Unit = {
    getOrCreateCollection(EARTH_EXPLORER_TOKENS)
      .updateMany(exists("token"), set("token", value))
      .results()
  }

  def addFieldToDoc(docId: String, field: String, value: BsonValue, collectionName: String): Unit = {
    getOrCreateCollection(collectionName).updateOne(equal("_id", docId), set(field, value)).results()
  }

  def getDocField(docId: String, field: String, collectionName: String): Future[Document] = {
    getOrCreateCollection(collectionName)
      .find(equal("_id", docId))
      .projection(excludeId())
      .first
      .toFuture()
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

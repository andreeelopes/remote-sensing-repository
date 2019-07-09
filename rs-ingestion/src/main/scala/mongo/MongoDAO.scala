package mongo

import com.mongodb.client.model.{Filters, IndexOptions, Indexes}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import mongo.Helpers._
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString, BsonValue}
import com.typesafe.config.{Config, ConfigFactory}
import play.api.libs.json.{JsArray, JsObject, JsValue, Json, __}
import sources.Extraction
import utils.Utils

import scala.collection.JavaConverters._
import scala.util.Try

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
  val EARTH_EXPLORER_AUTH = "eeAuth"

  val database: MongoDatabase = mongoClient.getDatabase(DB_NAME)

  //  database.drop().results() // TODO remove

  private val collections = Map(
    PRODUCTS_COL -> database.getCollection(PRODUCTS_COL),
    FETCHING_LOG_COL -> database.getCollection(FETCHING_LOG_COL),
    PERIODIC_FETCHING_LOG_COL -> database.getCollection(PERIODIC_FETCHING_LOG_COL),
    EARTH_EXPLORER_AUTH -> database.getCollection(EARTH_EXPLORER_AUTH),
  )

  insertDoc(BsonDocument("_id" -> "token", "token" -> "NA"), EARTH_EXPLORER_AUTH)
  insertDoc(BsonDocument("_id" -> "cookies", "cookies" -> BsonArray()), EARTH_EXPLORER_AUTH)

  createIndexes()


  def insertDoc(doc: BsonDocument, collectionName: String = PRODUCTS_COL): Unit = {
    try {
      collections(collectionName).insertOne(doc).results()
    }
    catch {
      case e: MongoException => println(e.getMessage)
    }
  }

  def updateUrl(extraction: Extraction, productId: String): Unit = {

    collections(PRODUCTS_COL)
      .updateOne(
        equal("_id", productId),
        set(s"data.${extraction.name}",
          BsonDocument("status" -> BsonString("local"), "url" -> BsonString(extraction.destPath)))
      )
      .results()
  }

  def updateToken(value: BsonValue): Unit = {
    collections(EARTH_EXPLORER_AUTH)
      .updateMany(exists("token"), set("token", value))
      .results()
  }

  def updateCookies(cookies: BsonArray): Unit = {
    collections(EARTH_EXPLORER_AUTH)
      .updateMany(exists("cookies"), set("cookies", cookies))
      .results()
  }

  def addFieldToDoc(docId: String, field: String, value: BsonValue, collectionName: String = PRODUCTS_COL): Unit = {
    collections(collectionName).updateOne(equal("_id", docId), set(field, value)).results()
  }

  def getDoc(docId: String, collectionName: String = PRODUCTS_COL): Option[BsonDocument] = {
    Try(collections(collectionName)
      .find(equal("_id", docId))
      .first
      .results()
      .head
      .toBsonDocument).toOption
  }

  def getLastPeriodicLogByProductType(productType: String): Option[BsonDocument] = {
    Try(collections(PERIODIC_FETCHING_LOG_COL)
      .find(equal("query.productType", productType))
      .sort(BsonDocument("query.endDate" -> -1))
      .first
      .results()
      .head
      .toBsonDocument).toOption
  }


  def updateProductData(productId: String, dataObjectId: String, updatedValue: JsObject, fromEarthExplorer: Boolean = false): Unit = {
    val doc = MongoDAO.getDoc(productId).get

    val docJson = Json.parse(doc.toJson)

    var indicator = true

    val imageryTransformer = __.read[JsArray].map {
      case JsArray(values) =>
        JsArray(values.map { e =>
          val idOpt = (e \ "_id").asOpt[String]
          if (fromEarthExplorer) {
            (e.as[JsObject] ++ updatedValue).as[JsValue]
          } else if (idOpt.isDefined && idOpt.get == dataObjectId) {
            (e.as[JsObject] ++ updatedValue).as[JsValue]
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
          val jsValue = (kv._2.as[JsObject] ++ updatedValue).as[JsValue]
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


  def createIndexes(): Unit = {

    val indexes = Utils.getIndexesConf("indexing.indexes")

    indexes.asScala.foreach { index =>
      index.indexType match {
        case "full" =>
          index.order match {
            case "ascending" => collections(PRODUCTS_COL).createIndex(Indexes.ascending(index.fields.head)).results()
            case "descending" => collections(PRODUCTS_COL).createIndex(Indexes.descending(index.fields.head)).results()
          }
        case "partial" =>
          val partialFilterIndexOptions = new IndexOptions().partialFilterExpression(Filters.exists(index.fields.head))
          index.order match {
            case "ascending" =>
              collections(PRODUCTS_COL).createIndex(Indexes.ascending(index.fields.head), partialFilterIndexOptions).results()
            case "descending" =>
              collections(PRODUCTS_COL).createIndex(Indexes.descending(index.fields.head), partialFilterIndexOptions).results()
          }

        case "geospatial" => collections(PRODUCTS_COL).createIndex(Indexes.geo2dsphere(index.fields.head)).results()
      }
    }

  }


}

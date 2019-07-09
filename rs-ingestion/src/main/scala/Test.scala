import java.nio.charset.StandardCharsets

import org.json.XML
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.locationtech.jts.io.gml2.GMLReader
import play.api.libs.json.{JsObject, JsValue, Json}

//import com.mongodb.async.client.MongoCollection
//import org.bson.BsonDocument
//import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
//import org.json.JSONObject
//import org.locationtech.jts.io.WKTReader
//import org.locationtech.jts.io.geojson.GeoJsonWriter
//import org.mongodb.scala.bson.BsonDocument
//import org.mongodb.scala.{Document, MongoClient, MongoDatabase}
//import play.api.libs.json.{JsObject, Json}
//
//import mongo.Helpers._
//
//////import mongo.MongoDAO.collectionFL
//////import org.mongodb.scala._
//////import org.mongodb.scala.bson.BsonArray
//////import org.mongodb.scala.model.Aggregates._
//////import org.mongodb.scala.model.Filters._
//////import org.mongodb.scala.model.Projections._
//////import org.mongodb.scala.model.Sorts._
//////import org.mongodb.scala.model.Updates._
//////import org.mongodb.scala.model._
////
//object Test extends App {
//
//
//
//}
//

//  val mongoClient: MongoClient = MongoClient()
//  Thread.sleep(1000)
//  val database: MongoDatabase = mongoClient.getDatabase("test")
//  val collection = database.getCollection("test")
//
//  collection.find().results()
//
//}
//
////  val a = "1"
////  val b = a.asInstanceOf[Int]
////  println(b)
//////
//////  val mongoClient: MongoClient = MongoClient()
//////  Thread.sleep(1000)
//////  val database: MongoDatabase = mongoClient.getDatabase("rs_db")
//////  val collection: MongoCollection[Document] = database.getCollection("fetching_log")
//////
//////
//////  collection.drop()
//////
//////
//////  collection.insertOne(Document("_id"-> "7111694dd8-8146-46ff-886d-ad2afe89e5c9", "le" -> )).results()
//////
//////
//////
////////
////////  //  Json.parse(
////////  //    s"""
////////  //    {
////////  //      "query" : {
////////  //        "name" : "${extraction.name}",
////////  //        "date" : "${new DateTime().toString(dateFormat)}",
////////  //        "url" : "$adaptedQuotesUrl",
////////  //        "expression" : "${extraction.query}",
////////  //        "context" : "${extraction.context}",
////////  //        "context-format" : "${extraction.contextFormat}"
////////  //      },
////////  //      "result" : {
////////  //        "type" : "${extraction.resultType}",
////////  //        $result
////////  //      },
////////  //      "metamodel-mapping" : "${extraction.metamodelMapping}"
////////  //    }""")
////////
////////    // make a document and insert it
////////    val doc: Document = Document("_id" -> 0, "name" -> "MongoDB", "type" -> "database",
////////      "count" -> 1, "info" -> Document("x" -> 203, "y" -> 102))
////////
////////
////////    collection.insertOne(doc).results()
////////
////////    // get it (since it's the only one in there since we dropped the rest earlier on)
////////    collection.find.first().printResults()
////////
////////    // now, lets add lots of little documents to the collection so we can explore queries and cursors
////////    val documents: IndexedSeq[Document] = (1 to 100) map { i: Int => Document("i" -> i) }
////////    val insertObservable = collection.insertMany(documents)
////////
////////    val insertAndCount = for {
////////      insertResult <- insertObservable
////////      countResult <- collection.countDocuments()
////////    } yield countResult
////////
////////    println(s"total # of documents after inserting 100 small ones (should be 101):  ${insertAndCount.headResult()}")
////////
////////    collection.find().first().printHeadResult()
////////
////////    // Query Filters
////////    // now use a query to get 1 document out
////////    collection.find(equal("i", 71)).first().printHeadResult()
////////
////////    // now use a range query to get a larger subset
////////    collection.find(gt("i", 50)).printResults()
////////
////////    // range query with multiple constraints
////////    collection.find(and(gt("i", 50), lte("i", 100))).printResults()
////////
////////    // Sorting
////////    collection.find(exists("i")).sort(descending("i")).first().printHeadResult()
////////
////////    // Projection
////////    collection.find().projection(excludeId()).first().printHeadResult()
////////
////////    //Aggregation
////////    collection.aggregate(Seq(
////////      filter(gt("i", 0)),
////////      project(Document("""{ITimes10: {$multiply: ["$i", 10]}}"""))
////////    )).printResults()
////////
////////
////////    // Update One
////////    collection.updateOne(equal("i", 10), set("i", 110)).printHeadResult("Update Result: ")
////////
////////    // Update Many
////////    collection.updateMany(lt("i", 100), inc("i", 100)).printHeadResult("Update Result: ")
////////
////////    // Delete One
////////    collection.deleteOne(equal("i", 110)).printHeadResult("Delete Result: ")
////////
////////    // Delete Many
////////    collection.deleteMany(gte("i", 100)).printHeadResult("Delete Result: ")
////////
////////    collection.drop().results()
////////
////////    // ordered bulk writes
////////    val writes: List[WriteModel[_ <: Document]] = List(
////////      InsertOneModel(Document("_id" -> 4)),
////////      InsertOneModel(Document("_id" -> 5)),
////////      InsertOneModel(Document("_id" -> 6)),
////////      UpdateOneModel(Document("_id" -> 1), set("x", 2)),
////////      DeleteOneModel(Document("_id" -> 2)),
////////      ReplaceOneModel(Document("_id" -> 3), Document("_id" -> 3, "x" -> 4))
////////    )
////////
////////    collection.bulkWrite(writes).printHeadResult("Bulk write results: ")
////////
////////    collection.drop().results()
////////
////////    collection.bulkWrite(writes, BulkWriteOptions().ordered(false)).printHeadResult("Bulk write results (unordered): ")
////////
////////    collection.find().printResults("Documents in collection: ")
////////
////////    // Clean up
////////    collection.drop().results()
////////
////////    // release resources
////////    mongoClient.close()
//////
////}

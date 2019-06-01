package mongo

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._


object MongoDAO {

  val mongoClient: MongoClient = MongoClient()
  Thread.sleep(1000)
  val database: MongoDatabase = mongoClient.getDatabase("rs_db")
  val collectionFL = database.getCollection("metamodel_collection ")

  collectionFL.drop

  //  def insertDoc(docStr: String, collectionName: String = "collectionFL") = {
  //    collectionFL.insertOne(Document.parse(docStr))
  //  }

  def insertDoc(doc: Document, collectionName: String = "collectionFL") = {
    collectionFL.insertOne(doc)
  }

  def addFieldToDoc(docId: String, field: String, value: String) = {
    collectionFL.updateOne(equal("_id", docId), set(field, value))
  }


}

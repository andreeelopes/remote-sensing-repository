package sources.handlers

import com.typesafe.config.{Config, ConfigFactory}
import mongo.MongoDAO
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}
import utils.Utils
import utils.Utils.generateUUID

object EarthExplorerUtils {


  val config: Config = ConfigFactory.load()

  val bands: Map[String, List[(String, Int, Int)]] = Utils.getBandsEE


  def getDataDoc(productType: String, productId: String, entityId: String): BsonDocument = {

    val productBands = bands(productType)
    val datasetNumber = (MongoDAO.sourcesJson \ "earth-explorer" \ productType \ "datasetNumber").as[Long]

    val url = s"https://earthexplorer.usgs.gov/download/$datasetNumber/$entityId/STANDARD/EE"

    BsonDocument(
      "imagery" -> BsonArray(
        productBands.map { b =>
          val filename = if (productType != "MODIS_MYD13Q1_V6") BsonString(s"${productId}_${b._1}.tif")
          else MongoDAO.getDoc(productId).get.getString("granuleId")

          BsonDocument(
            "_id" -> BsonString(generateUUID()),
            "url" -> BsonString(url),
            "band" -> BsonString(b._1.replace("b", "")),
            "resolution" -> BsonDocument(
              "x" -> b._2,
              "y" -> b._3
            ),
            "status" -> BsonString("remote"),
            "fileName" -> filename,
          )
        }
      ),
      "metadata" -> BsonArray()
    )
  }
}
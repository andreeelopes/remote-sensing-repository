package sources.handlers

import com.typesafe.config.{Config, ConfigFactory}
import mongo.MongoDAO
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonString}
import utils.Utils.generateUUID

object EarthExplorerUtils {


  val config: Config = ConfigFactory.load()

  val bands: Map[String, List[(String, Int, Int)]] = Map(
    "LANDSAT_8_C1" -> List(
      ("b1", 30, 30), ("b2", 30, 30), ("b3", 30, 30), ("b4", 30, 30), ("b5", 30, 30), ("b6", 30, 30), ("b7", 30, 30),
      ("b8", 15, 15), ("b9", 30, 30), ("b10", 100, 30), ("b11", 100, 30), ("bqa", 30, 30), ("pixel_qa", 30, 30),
    ),
    "LANDSAT_ETM_C1" -> List(
      ("b1", 30, 30), ("b2", 30, 30), ("b3", 30, 30), ("b4", 30, 30), ("b5", 30, 30), ("b61", 60, 60), ("b62", 60, 60), ("b7", 30, 30),
      ("b8", 15, 15), ("bqa", 30, 30), ("pixel_qa", 30, 30),
    ),
    "MODIS_MYD13Q1_V6" -> List(("multi-layer", 250, 250)
    )
  )


  def getDataDoc(productType: String, productId: String, entityId: String): BsonDocument = {

    val productBands = bands(productType)
    val datasetNumber = (MongoDAO.sourcesJson \ "earth-explorer" \ productType \ "datasetNumber").as[Long]

    val url = s"https://earthexplorer.usgs.gov/download/$datasetNumber/$entityId/STANDARD/EE"

    BsonDocument("imagery" ->
      BsonArray(
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
      )
    )
  }
}
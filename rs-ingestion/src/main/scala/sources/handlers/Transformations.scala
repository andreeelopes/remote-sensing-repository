package sources.handlers

import mongo.MongoDAO
import org.joda.time.DateTime
import org.locationtech.jts.geom.{Coordinate, CoordinateFilter, GeometryFactory}
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.locationtech.jts.io.gml2.GMLReader
import play.api.libs.json.{JsObject, JsValue, Json}
import sources.Extraction

object Transformations {


  def transform(productId: String, extraction: Extraction, value: Any): Any = {

    if (extraction.updateUrl)
      MongoDAO.updateUrl(extraction, productId)

    extraction.name match {
      case "footprint" => gmlToGeoJson(value.asInstanceOf[String])
      case "spatialFootprint" => removeFirstAndLast(value.asInstanceOf[JsValue])
      case "beginposition-dayofyear" |
           "endposition-dayofyear" |
           "start_time_dayofyear" |
           "stop_time_dayofyear" |
           "Acquisition Start Day of Year" |
           "Acquisition End Day of Year" => extractDayofYear(value.asInstanceOf[DateTime])
      case "size" => sizeToLong(value.asInstanceOf[String])
      case "granuleId" => extractGranuleIdFromName(value.asInstanceOf[String])
      case _ => value
    }
  }

  def extractGranuleIdFromName(productName: String): Any = {
    val split = productName.split("_").toList
    split(split.size - 2).drop(1)
  }


  def sizeToLong(sizeString: String): Any = {
    val split = sizeString.split(" ")

    split(1) match {
      case "GB" => (split(0).toFloat * 1024 * 1024 * 1024).toLong
      case "MB" => (split(0).toFloat * 1024 * 1024).toLong
      case "KB" => (split(0).toFloat * 1024).toLong
    }
  }

  def extractDayofYear(time: DateTime): Any = {
    time.dayOfYear().get().toLong
  }

  def wktToGeoJson(value: String): String = {
    val geometry = new WKTReader().read(value)
    val geojson = new GeoJsonWriter().write(geometry)

    (Json.parse(geojson).as[JsObject] - "crs").toString
  }


  class InvertCoordinateFilter extends CoordinateFilter {
    override def filter(coord: Coordinate): Unit = {
      val oldX = coord.x
      coord.x = coord.y
      coord.y = oldX
    }
  }

  def gmlToGeoJson(value: String): String = {
    var geometry = new GMLReader().read(value, new GeometryFactory())

    geometry.apply(new InvertCoordinateFilter())

    val geojson = new GeoJsonWriter().write(geometry)

    (Json.parse(geojson).as[JsObject] - "crs").toString
  }

  def removeFirstAndLast(value: JsValue): String = value.toString.substring(1, value.toString.length - 1)


}

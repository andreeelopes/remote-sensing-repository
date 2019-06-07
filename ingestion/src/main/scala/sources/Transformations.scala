package sources

import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.geojson.GeoJsonWriter
import play.api.libs.json.{JsObject, JsValue, Json}


object Transformations {

  def transform(extraction: Extraction, value: Any): Any = {
    extraction.name match {
      case "footprint" => footprintTransformation(value.asInstanceOf[String])
      case "spatialFootprint" => removeFirstAndLast(value.asInstanceOf[JsValue])
      case _ => value
    }
  }

  def footprintTransformation(value: String): String = {
    val geometry = new WKTReader().read(value)
    val geojson = new GeoJsonWriter().write(geometry)

    (Json.parse(geojson).as[JsObject] - "crs").toString
  }

  def removeFirstAndLast(value: JsValue): String = value.toString.substring(1, value.toString.length - 1)


}






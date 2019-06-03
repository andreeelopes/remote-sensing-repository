package sources

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.geojson.GeoJsonWriter
import play.api.libs.json.JsValue
import utils.Utils


object Transformations {
  val dtf1 = DateTimeFormat.forPattern("YYYY:DDD:HH:mm:ss.SSSSSSS")
  val dtf2 = DateTimeFormat.forPattern("YYYY-MM-dd")

  def transform(extraction: Extraction, value: Any) = {
    extraction.name match {
      case "footprint" => Left(footprintTransformation(value.asInstanceOf[String]))
      case "spatialFootprint" => Left(removeFirstAndLast(value.asInstanceOf[JsValue]))
      case "stop_time" | "start_time" | "Acquisition Start Date" | "Acquisition End Date" =>
        Left(standardizeDate(value.asInstanceOf[String], dtf1))
      case "acquisitionDate" => Left(standardizeDate(value.asInstanceOf[String], dtf2))
      case _ => value
    }
  }

  def footprintTransformation(value: String) = {
    val geometry = new WKTReader().read(value)
    val geojson = new GeoJsonWriter().write(geometry)
    geojson
  }

  def removeFirstAndLast(value: String) = value.substring(1, value.length - 1)

  def standardizeDate(value: String, dtf: DateTimeFormatter) = {
    val date = dtf.parseDateTime(value) // TODO deal with timezones
    date.toString(Utils.dateFormat)
  }


}






package sources

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.geojson.GeoJsonWriter
import utils.Utils


object Transformations {
  val dtf1 = DateTimeFormat.forPattern("YYYY:DDD:HH:mm:ss.SSSSSSS")
  val dtf2 = DateTimeFormat.forPattern("YYYY-MM-dd")

  def transform(extraction: Extraction, value: Any): Either[List[String], Array[Byte]] = {
    extraction.name match {
      case "footprint" => Left(List(footprintTransformation(value.asInstanceOf[String])))
      case "spatialFootprint" => Left(List(removeFirstAndLast(value.asInstanceOf[String])))
      case "stop_time" | "start_time" | "Acquisition Start Date" | "Acquisition End Date" =>
        Left(List(standardizeDate(value.asInstanceOf[String], dtf1)))
      case "acquisitionDate" => Left(List(standardizeDate(value.asInstanceOf[String], dtf2)))

      case _ => value match {
        case a: Array[Byte] => Right(a)
        case l: List[String] => Left(l)
        case _ => Left(List(value.toString))
      }
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






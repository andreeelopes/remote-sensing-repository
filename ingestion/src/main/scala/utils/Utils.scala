package utils

import java.util.UUID

object Utils {

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"


  def generateWorkId(): String = UUID.randomUUID().toString

}

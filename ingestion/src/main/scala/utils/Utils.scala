package utils

import java.util.UUID

object Utils {

  def generateWorkId(): String = UUID.randomUUID().toString

}

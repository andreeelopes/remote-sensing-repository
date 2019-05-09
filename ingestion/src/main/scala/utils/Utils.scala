package utils

import java.io.{File, PrintWriter}
import java.util.UUID


trait KryoSerializable

object Utils {

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

  def generateUUID(): String = UUID.randomUUID().toString


  def writeFile(filename: String, content: String) = {
    val pw = new PrintWriter(new File(filename))
    pw.write(content)
    pw.close()
  }

}

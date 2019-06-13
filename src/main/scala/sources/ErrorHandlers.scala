package sources

import java.nio.charset.StandardCharsets

object ErrorHandlers {

  def copernicusODataErrorHandler(status: Int, body: Array[Byte], statusText: String): Unit = {
    if (status == 500) {
      val responseStr = new String(body, StandardCharsets.UTF_8)
      if (responseStr.contains("Unexpected nav segment Navigation Property")) // resource does not exist, dont try again
        println("Resource doesnt exist, dont retry") // TODO log error
    }
    else if (status == 403 /*rate limit*/ || status == 500)
      throw new Exception(statusText + "->\n" + new String(body))
  }

  def earthExplorerErrorHandler(status: Int, body: Array[Byte], statusText: String): Unit = {
    if (status == 200) {
      val responseStr = new String(body, StandardCharsets.UTF_8) // TODO try can be too big
      if (responseStr.contains("RATE_LIMIT"))
        throw new Exception(statusText + "->\n" + new String(body))

      //token
    }
    else if (status == 500)
      throw new Exception(statusText + "->\n" + new String(body))
  }


  def defaultErrorHandler(status: Int, body: Array[Byte], statusText: String): Unit = {
    if (status == 500)
      throw new Exception(statusText + "->\n" + new String(body))
  }

}



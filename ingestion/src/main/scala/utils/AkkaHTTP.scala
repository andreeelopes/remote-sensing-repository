package utils

import akka.actor.ActorContext
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, headers}
import akka.stream.ActorMaterializer
import sources.AuthConfig

import scala.concurrent.Future

object AkkaHTTP {

  // TODO streaming for big objects
  def singleRequest(url: String, authConfigOpt: Option[AuthConfig])
                   (implicit context: ActorContext, mat: ActorMaterializer): Future[HttpResponse] = {
    println(s"Starting to fetch: $url") // TODO logs

    val request = if (authConfigOpt.isDefined) {
      val authConfig = authConfigOpt.get
      val authorization =
        List(headers.Authorization(BasicHttpCredentials(authConfig.username, authConfig.password)))

      HttpRequest(uri = url, headers = authorization)
    } else
      HttpRequest(uri = url)


    Http(context.system).singleRequest(request)
  }


}

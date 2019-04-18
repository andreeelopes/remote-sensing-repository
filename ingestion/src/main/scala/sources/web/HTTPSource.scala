package sources.web

import akka.actor.ActorContext
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, headers}
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import sources.{AuthComponent, Source, Work}

import scala.concurrent.Future


abstract class HTTPSource(configName: String, config: Config)
  extends Source(configName, config) with AuthComponent {

  val baseUrl = config.getString(s"sources.$configName.base-url")

}

abstract class HTTPWork(override val source: HTTPSource) extends Work(source) {

  val url: String

  // TODO streaming for big objects
  def httpRequest()(implicit context: ActorContext, mat: ActorMaterializer): Future[HttpResponse] = {
    val request = if (source.authConfigOpt.isDefined) {
      val authConfig = source.authConfigOpt.get
      val authorization =
        List(headers.Authorization(BasicHttpCredentials(authConfig.username, authConfig.password)))

      HttpRequest(uri = url, headers = authorization)
    } else
      HttpRequest(uri = url)


    Http(context.system).singleRequest(request)
  }

}

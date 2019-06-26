package api

import java.time

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, concat, entity, onSuccess, pathEnd, pathPrefix, rejectEmptyResponse}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import akka.pattern.ask
import api.Services.{ActionPerformed, FetchData}
import utils.JsonSupport

import scala.concurrent.duration._
import scala.concurrent.Future

//#user-routes-class
trait Routes extends JsonSupport {
  //#user-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[Routes])

  // other dependencies that Routes use
  def apiServerActor: ActorRef

  // Required by the `ask` (?) method below
  val timeoutConf: FiniteDuration = ConfigFactory.load().getDuration("api.ask-timeout").getSeconds.seconds
  implicit lazy val timeout: Timeout = Timeout(timeoutConf)

  lazy val serverRoutes: Route =
    pathPrefix("work") {
      concat(
        //#users-get-delete
        pathEnd {
          concat(
            post {
              entity(as[FetchData]) { fetchData =>
                val actionPerformed = (apiServerActor ? fetchData).mapTo[ActionPerformed]

                onSuccess(actionPerformed) { performed =>
                  log.info(performed.description)
                  complete((StatusCodes.getForKey(performed.statusCode).get, performed.description))
                }
              }
            }
          )
        },
      )
    }
}

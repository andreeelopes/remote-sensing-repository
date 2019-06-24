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

import scala.concurrent.duration._
import scala.concurrent.Future

//#user-routes-class
trait Routes {
  //#user-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[Routes])

  // other dependencies that Routes use
  def apiServerActor: ActorRef

  // Required by the `ask` (?) method below
  val timeoutConf: FiniteDuration = ConfigFactory.load().getDuration("api.ask-timeout").getSeconds.seconds
  implicit lazy val timeout: Timeout = Timeout(timeoutConf)

  //#all-routes
  //#users-get-post
  //#users-get-delete
  lazy val serverRoutes: Route =
  pathPrefix("work") {
    concat(
      //#users-get-delete
      pathEnd {
        concat(
          get {
            val helloback = (apiServerActor ? "hello").mapTo[String]

            complete(helloback)
          },
          //          post {
          //            entity(as[User]) { user =>
          //              val userCreated: Future[ActionPerformed] =
          //                (userRegistryActor ? CreateUser(user)).mapTo[ActionPerformed]
          //              onSuccess(userCreated) { performed =>
          //                log.info("Created user [{}]: {}", user.name, performed.description)
          //                complete((StatusCodes.Created, performed))
          //              }
          //            }
          //          }
        )
      },
      //      //#users-get-post
      //      //#users-get-delete
      //      path(Segment) { name =>
      //        concat(
      //          get {
      //            //#retrieve-user-info
      //            val maybeUser: Future[Option[User]] =
      //              (userRegistryActor ? GetUser(name)).mapTo[Option[User]]
      //            rejectEmptyResponse {
      //              complete(maybeUser)
      //            }
      //            //#retrieve-user-info
      //          },
      //          delete {
      //            //#users-delete-logic
      //            val userDeleted: Future[ActionPerformed] =
      //              (userRegistryActor ? DeleteUser(name)).mapTo[ActionPerformed]
      //            onSuccess(userDeleted) { performed =>
      //              log.info("Deleted user [{}]: {}", name, performed.description)
      //              complete((StatusCodes.OK, performed))
      //            }
      //            //#users-delete-logic
      //          }
      //        )
      //      }
    )
    //#users-get-delete
  }
  //#all-routes
}

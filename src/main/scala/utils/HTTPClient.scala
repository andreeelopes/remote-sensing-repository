package utils

import sources.Work

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.libs.ws.ahc._
import play.api.libs.ws._
import DefaultBodyReadables._
import akka.actor.{ActorContext, ActorRef}
import akka.stream.ActorMaterializer
import protocol.worker.WorkExecutor.WorkComplete
import sources.handlers.AuthConfig

import scala.concurrent.duration._

object HTTPClient {

  def singleRequest(url: String,
                    timeout: Duration,
                    process: Array[Byte] => List[Work],
                    errorHandler: (Int, Array[Byte], String, ActorMaterializer) => Unit,
                    authConfigOpt: Option[AuthConfig] = None)
                   (implicit context: ActorContext, mat: ActorMaterializer): Unit = {

    implicit val origSender: ActorRef = context.sender

    val wsClient = StandaloneAhcWSClient()

    val wsClientUrl = wsClient.url(url).withRequestFilter(AhcCurlRequestLogger())

    val wsClientAuth =
      if (authConfigOpt.isDefined)
        wsClientUrl.withAuth(authConfigOpt.get.username, authConfigOpt.get.password, WSAuthScheme.BASIC)
      else wsClientUrl

    wsClientAuth
      .withRequestTimeout(timeout)
      .get
      .map { response =>
        val body = response.body[Array[Byte]]

        errorHandler(response.status, body, response.statusText, mat)

        val workToBeDone = process(body)

        origSender ! WorkComplete(workToBeDone)
      }
      .recover { case e: Exception => context.self ! e }
      .andThen { case _ => wsClient.close() }
  }


}





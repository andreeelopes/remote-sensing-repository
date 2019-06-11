package utils

import sources.{AuthConfig, Work}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.libs.ws.ahc._
import play.api.libs.ws._
import DefaultBodyReadables._
import akka.actor.{ActorContext, ActorRef}
import akka.stream.ActorMaterializer
import protocol.worker.WorkExecutor.WorkComplete

import scala.concurrent.duration._

object HTTPClient {

  def singleRequest(url: String,
                    timeout: Duration,
                    process: Array[Byte] => List[Work],
                    //                    processErrors: Array[Byte] => List[Work] = () => List(),
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

        println(s"Response status - ${response.status}")

        if (!(200 to 299).contains(response.status))
          println(s"The response header Content-Length was ${response.header("Content-Length")}")

        val body = response.body[Array[Byte]]

        val workToBeDone = process(body)

        origSender ! WorkComplete(workToBeDone)
      }
      .recover { case e: Exception => context.self ! e }
      .andThen { case _ => wsClient.close() }
  }


}





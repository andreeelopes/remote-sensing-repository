package api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ServerStart(port: Int, serverActor: ActorRef, actorSystem: ActorSystem) extends Routes {

  val config: Config = ConfigFactory.load()

  override implicit def system: ActorSystem = actorSystem

  override def apiServerActor: ActorRef = serverActor

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  lazy val routes: Route = serverRoutes

  val host: String = config.getString("clustering.ip")

  //#http-server
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, host, 8080) // TODO external ip

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

}


package sources

import java.io.PrintWriter

import akka.actor.ActorContext
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.{HttpRequest, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import commons.Work
import worker.WorkExecutor.WorkComplete

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class CopernicusSource(implicit actorContext: ActorContext) extends Source(configName = "copernicusOAH") {
  override def generateWork() = new CopernicusWork(this)
}


class CopernicusWork(src: Source) extends Work(src: Source) {

  def execute(implicit context: ActorContext, actorMat: ActorMaterializer) = {

    val authorization = headers.Authorization(BasicHttpCredentials("andrelopes", "andrelopez14"))
    val request = HttpRequest(uri = source.url, headers = List(authorization))

    val origSender = context.sender

    Http(context.system).singleRequest(request).onComplete {

      case Success(response) =>

        Unmarshal(response.entity).to[String].onComplete {

          case Success(value) =>

            //              val xmlElem = scala.xml.XML.loadString(value)
            //              //
            //              //              val lastEndpoint2 = (xmlElem \ "_").filter(node => node.attribute("rel")
            //              //                .exists(rel => rel.text.equals("last")))
            //              //
            //              //
            //              //              log.info(lastEndpoint2.toString())

            new PrintWriter(s"metadata$workId.xml") {
              try write(value) finally close()
            }

            origSender ! WorkComplete("lala")

          case Failure(e) => // log.info(e.getMessage)
        }

      case Failure(e) => //log.info(e.getMessage)

    }

  }

}
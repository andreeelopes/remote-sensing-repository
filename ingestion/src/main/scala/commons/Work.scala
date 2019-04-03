package commons

import java.io.PrintWriter

import akka.actor.ActorContext
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, headers}
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import sources.Source
import utils.Utils
import worker.WorkExecutor.WorkComplete
import scala.concurrent.ExecutionContext.Implicits.global


import scala.util.{Failure, Success}

case class WorkResult(workId: String, result: Any)


//@SerialVersionUID(1L)
class Work(src: Source) extends Serializable {

  val source = src

  val workId = Utils.generateWorkId()


  def execute(implicit context: ActorContext, actorMat: ActorMaterializer) = {

    val authorization = headers.Authorization(BasicHttpCredentials("andrelopes", "andrelopez14"))
    val request = HttpRequest(uri = source.url, headers = List(authorization))

    val origSender = context.sender


    Http(context.system).singleRequest(request).onComplete {

      case Success(response) =>

        Unmarshal(response.entity).to[String].onComplete {

          case Success(value) =>

            val xmlElem = scala.xml.XML.loadString(value)

            val links = xmlElem.child.filter(node => node.label.equals("link"))
            val link = links.filter(node => (node \@ "rel").equals("next")).head
            val next = link \@ "href"


            new PrintWriter(s"metadata$workId.xml") {
              try write(value) finally close()
            }

            origSender ! WorkComplete(next)


          case Failure(e) => // log.info(e.getMessage)
        }

      case Failure(e) => //log.info(e.getMessage)

    }

  }


}


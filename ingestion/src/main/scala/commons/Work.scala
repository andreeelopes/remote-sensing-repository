package commons

import java.io.PrintWriter

import akka.actor.{ActorContext, ActorRef}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, ResponseEntity, headers}
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import sources.Source
import utils.Utils
import worker.WorkExecutor.WorkComplete

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.xml.{Elem, XML}

case class WorkResult(workId: String, result: Any)


class Work(src: Source) extends Serializable {

  val source = src

  val workId = Utils.generateWorkId()


  def execute(implicit context: ActorContext, actorMat: ActorMaterializer) = {

    val authorization = headers.Authorization(BasicHttpCredentials(source.username, source.password))
    val request = HttpRequest(uri = source.url, headers = List(authorization))

    implicit val origSender = context.sender

    Http(context.system).singleRequest(request).onComplete {

      case Success(response) => unmarshal(response)
      case Failure(e) => throw new Exception(e.getMessage)

    }

  }

  def unmarshal(response: HttpResponse)(implicit actorMat: ActorMaterializer, origSender: ActorRef) = {

    Unmarshal(response.entity).to[String].onComplete {

      case Success(value) =>

        val processedXML = preProcess(value)

        new PrintWriter(s"metadata$workId.xml") {
          try write(processedXML.toString) finally close()
        }

        origSender ! WorkComplete("DONE") // TODO o que retornar?


      case Failure(e) => throw new Exception(e.getMessage)
    }

  }

  def preProcess(metadata: String): String = metadata


}


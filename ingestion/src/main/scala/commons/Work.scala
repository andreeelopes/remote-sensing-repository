package commons

import akka.actor.{ActorContext, ActorRef}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, ResponseEntity, headers}
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.stream.ActorMaterializer
import org.joda.time.DateTime
import sources.Source
import utils.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

case class WorkResult(workId: String, result: Any)


trait KryoSerializable

class Work(val source: Source, val ingestionDates: (DateTime, DateTime), val isEpoch: Boolean = false,
           val pageStart: Int = 0) extends KryoSerializable {


  val workId = Utils.generateWorkId()
  val url = source.generateQuery(pageStart, ingestionDates)

  println(s"Starting to fetch: $url") // TODO logs

  def execute(implicit context: ActorContext, actorMat: ActorMaterializer) = {

    val authorization = headers.Authorization(BasicHttpCredentials(source.username, source.password))
    val request = HttpRequest(uri = url, headers = List(authorization))

    implicit val origSender = context.sender

    Http(context.system).singleRequest(request).onComplete {

      case Success(response) => unmarshal(response)
      case Failure(e) => throw new Exception(e.getMessage)

    }

  }



  def unmarshal(response: HttpResponse)(implicit actorMat: ActorMaterializer, origSender: ActorRef) = {}

  def preProcess(metadata: String): String = metadata

  def getNext(metadata: String): List[Work] = List()


}


//package sources
//
//import akka.actor.ActorContext
//import akka.http.scaladsl.unmarshalling.Unmarshal
//import akka.stream.ActorMaterializer
//import com.typesafe.config.Config
//import protocol.worker.WorkExecutor.WorkComplete
//import sources.web.HTTPWork
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.util.{Failure, Success}
//import scala.xml.{Elem, XML}
//
////Todo ter em conta tamanho do download - stream vs single request
//class CopernicusODataSource(config: Config) extends HTTPSource("copernicusOAHOData", config) {
//  override val authConfigOpt = Some(AuthConfig("copernicusOAHOData", config)) //TODO não pode estar sempre a ir ao disco
//
//  // TODO timeout based on the size of object
//}
//
//
//class CopernicusODataWork(override val source: CopernicusODataSource, override val url: String)
//  extends HTTPWork(source) {
//
//  var resource: Elem = _
//
//  override def execute()(implicit context: ActorContext, mat: ActorMaterializer) = {
//
//
//    implicit val origSender = context.sender
//
//    httpRequest.onComplete {
//      case Success(response) =>
//
//        Unmarshal(response.entity).to[String].onComplete {
//          case Success(responseString) =>
//            resource = XML.loadString(responseString)
//
//            println(resource)
//
//            val workToBeDone = List()
//            origSender ! WorkComplete(workToBeDone)
//
//
//          case Failure(e) => throw new Exception(e)
//        }
//      case Failure(e) => throw new Exception(e)
//    }
//
//
//  }
//
//}
//
//
//

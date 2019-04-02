package sources

import akka.actor.ActorContext
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import commons.Work

class CopernicusSource(config: Config) extends Source("copernicusOAH", config) {
  override def generateWork() = new CopernicusWork(this)
}

class CopernicusWork(source: CopernicusSource) extends Work(source){

  override def execute(implicit context: ActorContext, actorMat: ActorMaterializer) = println("AQUI")
}
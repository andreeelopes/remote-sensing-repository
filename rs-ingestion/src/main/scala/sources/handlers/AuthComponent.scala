package sources.handlers

import com.typesafe.config.Config
import mongo.MongoDAO

trait AuthComponent {
  val authConfigOpt: Option[AuthConfig]
}

case class AuthConfig(sourceName: String, config: Config) {
  val username: String = (MongoDAO.sourcesJson \ sourceName \ "credentials" \ "username").as[String]
  val password: String = (MongoDAO.sourcesJson \ sourceName \ "credentials" \ "pwd").as[String]
}






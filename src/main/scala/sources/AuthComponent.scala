package sources

import com.typesafe.config.Config

trait AuthComponent {
  val authConfigOpt: Option[AuthConfig]
}

case class AuthConfig(sourceName: String, config: Config) {
  val username: String = config.getString(s"sources.$sourceName.credentials.username")
  val password: String = config.getString(s"sources.$sourceName.credentials.pwd")
}






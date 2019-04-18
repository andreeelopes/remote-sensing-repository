package sources

import com.typesafe.config.Config

trait AuthComponent {
  val authConfigOpt: Option[AuthConfig]
}

case class AuthConfig(sourceName: String, config: Config) {
  val username = config.getString(s"sources.$sourceName.credentials.username")
  val password = config.getString(s"sources.$sourceName.credentials.pwd")
}






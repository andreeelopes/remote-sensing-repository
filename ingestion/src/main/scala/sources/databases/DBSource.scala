package sources.databases

import com.typesafe.config.Config
import sources.Source

abstract class DBSource(configName: String, config: Config) extends Source(configName, config) {

}

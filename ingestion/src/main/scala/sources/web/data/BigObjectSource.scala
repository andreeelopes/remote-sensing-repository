package sources.web.data

import com.typesafe.config.Config
import sources.web.HTTPSource

abstract class BigObjectSource(configName: String, config: Config) extends HTTPSource(configName, config) {

}

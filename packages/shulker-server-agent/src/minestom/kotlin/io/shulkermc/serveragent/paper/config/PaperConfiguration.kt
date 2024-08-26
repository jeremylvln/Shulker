package io.shulkermc.serveragent.paper.config

import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream

data class VelocityConfiguration(
    val enabled: Boolean = false,
    val secret: String? = null,
)

data class ProxiesConfiguration(
    val velocity: VelocityConfiguration = VelocityConfiguration(),
)

data class PaperConfiguration(
    val proxies: ProxiesConfiguration = ProxiesConfiguration(),
) {
    companion object {
        private val CONFIG_FILE_PATH = Paths.get("config/paper-global.yml")
        private val DEFAULT_CONFIG = PaperConfiguration()

        fun load(): PaperConfiguration {
            if (!CONFIG_FILE_PATH.exists()) {
                return DEFAULT_CONFIG
            }

            val yaml = Yaml(Constructor(PaperConfiguration::class.java, LoaderOptions()))
            return yaml.load(CONFIG_FILE_PATH.inputStream()) as PaperConfiguration
        }
    }
}

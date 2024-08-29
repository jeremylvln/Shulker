package io.shulkermc.serveragent.minestom.config

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.representer.Representer
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.inputStream

data class VelocityConfiguration(
    var enabled: Boolean = false,
    var secret: String? = null,
)

data class ProxiesConfiguration(
    var velocity: VelocityConfiguration = VelocityConfiguration(),
)

data class PaperConfiguration(
    var proxies: ProxiesConfiguration = ProxiesConfiguration(),
) {
    companion object {
        private val CONFIG_FILE_PATH = Paths.get("config/paper-global.yml")
        private val DEFAULT_CONFIG = PaperConfiguration()

        fun load(): PaperConfiguration {
            if (!CONFIG_FILE_PATH.exists()) {
                return DEFAULT_CONFIG
            }

            val representer = Representer(DumperOptions())
            representer.propertyUtils.isSkipMissingProperties = true

            val yaml = Yaml(Constructor(PaperConfiguration::class.java, LoaderOptions()), representer)
            return yaml.load(CONFIG_FILE_PATH.inputStream()) as PaperConfiguration
        }
    }
}

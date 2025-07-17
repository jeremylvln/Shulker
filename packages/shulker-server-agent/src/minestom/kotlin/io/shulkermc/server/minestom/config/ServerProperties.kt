package io.shulkermc.server.minestom.config

import java.nio.file.Paths
import java.util.Properties
import kotlin.io.path.exists
import kotlin.io.path.inputStream

data class ServerProperties(
    val serverIp: String = "0.0.0.0",
    val serverPort: Int = 25565,
) {
    companion object {
        private val CONFIG_FILE_PATH = Paths.get("server.properties")
        private val DEFAULT_PROPERTIES = ServerProperties()

        fun load(): ServerProperties {
            if (!CONFIG_FILE_PATH.exists()) {
                return DEFAULT_PROPERTIES
            }

            val properties = Properties()
            properties.load(CONFIG_FILE_PATH.inputStream())

            return ServerProperties(
                properties.getProperty("server-ip") ?: DEFAULT_PROPERTIES.serverIp,
                properties.getProperty("server-port")?.toInt() ?: DEFAULT_PROPERTIES.serverPort,
            )
        }
    }
}

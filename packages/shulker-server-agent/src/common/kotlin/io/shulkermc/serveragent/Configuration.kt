package io.shulkermc.serveragent

data class Configuration(
    val serverNamespace: String,
    val serverName: String
) {
    companion object {
        fun load(): Configuration {
            val serverNamespace = System.getenv("SHULKER_SERVER_NAMESPACE")
                ?: throw IllegalStateException("No SHULKER_SERVER_NAMESPACE found in environment")

            val serverName = System.getenv("SHULKER_SERVER_NAME")
                ?: throw IllegalStateException("No SHULKER_SERVER_NAME found in environment")

            return Configuration(
                serverNamespace,
                serverName,
            )
        }
    }
}

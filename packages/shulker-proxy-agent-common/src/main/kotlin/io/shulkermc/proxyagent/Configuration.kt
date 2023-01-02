package io.shulkermc.proxyagent

data class Configuration(
        val proxyNamespace: String,
        val proxyName: String,
        val ttlSeconds: Long
)

fun parse(): Configuration {
    val proxyNamespace = System.getenv("SHULKER_PROXY_NAMESPACE")
            ?: throw IllegalStateException("No SHULKER_PROXY_NAMESPACE found in environment")

    val proxyName = System.getenv("SHULKER_PROXY_NAME")
            ?: throw IllegalStateException("No SHULKER_PROXY_NAME found in environment")

    val ttlSecondsStr = System.getenv("SHULKER_PROXY_TTL_SECONDS")
            ?: throw IllegalStateException("No SHULKER_PROXY_TTL_SECONDS found in environment")
    val ttlSeconds = ttlSecondsStr.toLong()

    return Configuration(
            proxyNamespace,
            proxyName,
            ttlSeconds
    )
}

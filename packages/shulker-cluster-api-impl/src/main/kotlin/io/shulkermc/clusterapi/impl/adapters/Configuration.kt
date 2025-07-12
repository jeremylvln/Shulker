package io.shulkermc.clusterapi.impl.adapters

import java.util.Optional

@SuppressWarnings("detekt:MagicNumber")
object Configuration {
    val REDIS_HOST = getStringEnv("SHULKER_PROXY_REDIS_HOST")
    val REDIS_PORT = getIntEnv("SHULKER_PROXY_REDIS_PORT")
    val REDIS_USERNAME = getOptionalStringEnv("SHULKER_PROXY_REDIS_USERNAME")
    val REDIS_PASSWORD = getOptionalStringEnv("SHULKER_PROXY_REDIS_PASSWORD")

    private fun getStringEnv(name: String): String = requireNotNull(System.getenv(name)) { "Missing $name" }

    private fun getOptionalStringEnv(name: String): Optional<String> = Optional.ofNullable(System.getenv(name))

    private fun getIntEnv(name: String): Int = getStringEnv(name).toInt()
}

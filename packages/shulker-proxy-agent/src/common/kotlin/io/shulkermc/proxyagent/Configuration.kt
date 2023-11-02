package io.shulkermc.proxyagent

import java.util.UUID

object Configuration {
    val PROXY_NAMESPACE = getStringEnv("SHULKER_PROXY_NAMESPACE")
    val PROXY_NAME = getStringEnv("SHULKER_PROXY_NAME")
    val PROXY_TTL_SECONDS = getLongEnv("SHULKER_PROXY_TTL_SECONDS")

    val NETWORK_ADMINS = (getOptionalStringEnv("SHULKER_NETWORK_ADMINS") ?: "")
        .split(",")
        .map(UUID::fromString)

    val REDIS_HOST = getStringEnv("SHULKER_PROXY_REDIS_HOST")
    val REDIS_PORT = getIntEnv("SHULKER_PROXY_REDIS_PORT")
    val REDIS_USERNAME = getOptionalStringEnv("SHULKER_PROXY_REDIS_USERNAME")
    val REDIS_PASSWORD = getOptionalStringEnv("SHULKER_PROXY_REDIS_PASSWORD")

    private fun getStringEnv(name: String): String = requireNotNull(System.getenv(name)) { "Missing $name" }
    private fun getOptionalStringEnv(name: String): String? = System.getenv(name)
    private fun getIntEnv(name: String): Int = getStringEnv(name).toInt()
    private fun getLongEnv(name: String): Long = getStringEnv(name).toLong()
}

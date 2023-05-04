package io.shulkermc.proxyagent

object Configuration {
    val PROXY_NAMESPACE = requireNotNull(System.getenv("SHULKER_PROXY_NAMESPACE")) { "Missing SHULKER_PROXY_NAMESPACE" }
    val PROXY_NAME = requireNotNull(System.getenv("SHULKER_PROXY_NAME")) { "Missing SHULKER_PROXY_NAME" }
    val PROXY_TTL_SECONDS = requireNotNull(System.getenv("SHULKER_PROXY_TTL_SECONDS")) { "Missing SHULKER_PROXY_TTL_SECONDS" }.toLong()
}

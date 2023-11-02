package io.shulkermc.serveragent

import java.util.UUID

object Configuration {
    val NETWORK_ADMINS = (getOptionalStringEnv("SHULKER_NETWORK_ADMINS") ?: "")
        .split(",")
        .map(UUID::fromString)

    private fun getOptionalStringEnv(name: String): String? = System.getenv(name)
}

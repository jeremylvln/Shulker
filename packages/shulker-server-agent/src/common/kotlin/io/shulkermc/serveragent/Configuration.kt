package io.shulkermc.serveragent

import java.util.UUID

object Configuration {
    val NETWORK_ADMINS = getStringEnv("SHULKER_NETWORK_ADMINS").split(",").map(UUID::fromString)

    private fun getStringEnv(name: String): String = requireNotNull(System.getenv(name)) { "Missing $name" }
}

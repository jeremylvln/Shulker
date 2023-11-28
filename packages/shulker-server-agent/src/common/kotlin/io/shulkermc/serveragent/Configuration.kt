package io.shulkermc.serveragent

import java.util.Optional
import java.util.UUID

object Configuration {
    val NETWORK_ADMINS: List<UUID> = getOptionalStringEnv("SHULKER_NETWORK_ADMINS")
        .map {
            it.split(",")
                .filter(String::isNotBlank)
                .map(UUID::fromString)
        }
        .orElse(emptyList())

    private fun getOptionalStringEnv(name: String): Optional<String> = Optional.ofNullable(System.getenv(name))
}

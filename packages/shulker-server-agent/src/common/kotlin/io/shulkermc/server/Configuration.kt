package io.shulkermc.server

import java.util.Optional
import java.util.UUID

object Configuration {
    val NETWORK_ADMINS: List<UUID> =
        getOptionalStringEnv("SHULKER_NETWORK_ADMINS")
            .map {
                it.split(",")
                    .filter(String::isNotBlank)
                    .map(UUID::fromString)
            }
            .orElse(emptyList())

    val LIFECYCLE_STRATEGY: LifecycleStrategy =
        getOptionalStringEnv("SHULKER_SERVER_LIFECYCLE_STRATEGY")
            .map { value -> LifecycleStrategy.byEnvValue(value) }
            .orElse(LifecycleStrategy.ALLOCATE_WHEN_NOT_EMPTY)

    private fun getOptionalStringEnv(name: String): Optional<String> = Optional.ofNullable(System.getenv(name))

    enum class LifecycleStrategy(private val strategy: String) {
        ALLOCATE_WHEN_NOT_EMPTY("AllocateWhenNotEmpty"),
        MANUAL("Manual"),
        ;

        companion object {
            fun byEnvValue(value: String): LifecycleStrategy {
                return requireNotNull(LifecycleStrategy.entries.find { it.strategy == value }) {
                    "Unknown lifecycle strategy: $value"
                }
            }
        }
    }
}

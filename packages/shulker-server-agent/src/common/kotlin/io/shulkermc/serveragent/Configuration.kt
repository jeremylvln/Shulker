package io.shulkermc.serveragent

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

    val REDIS_HOST = getStringEnv("SHULKER_SERVER_REDIS_HOST")
    val REDIS_PORT = getIntEnv("SHULKER_SERVER_REDIS_PORT")
    val REDIS_USERNAME = getOptionalStringEnv("SHULKER_SERVER_REDIS_USERNAME")
    val REDIS_PASSWORD = getOptionalStringEnv("SHULKER_SERVER_REDIS_PASSWORD")

    val SERVER_NAMESPACE = getStringEnv("SHULKER_SERVER_NAMESPACE")

    private fun getStringEnv(name: String): String = requireNotNull(System.getenv(name)) { "Missing $name" }

    private fun getOptionalStringEnv(name: String): Optional<String> = Optional.ofNullable(System.getenv(name))

    private fun getIntEnv(name: String): Int = getStringEnv(name).toInt()

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

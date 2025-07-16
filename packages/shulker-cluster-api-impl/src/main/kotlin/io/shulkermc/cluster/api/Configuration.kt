package io.shulkermc.cluster.api

import redis.clients.jedis.JedisPool
import java.util.Optional
import kotlin.jvm.optionals.getOrDefault

@SuppressWarnings("detekt:MagicNumber")
data class Configuration(
    val clusterName: String,
    val owningFleetName: Optional<String>,
    val redis: Redis
) {
    data class Redis(
        val host: String,
        val port: Int,
        val username: Optional<String>,
        val password: Optional<String>
    ) {
        fun createJedisPool(): JedisPool {
            if (this.username.isPresent && this.password.isPresent) {
                return JedisPool(
                    this.host,
                    this.port,
                    this.username.get(),
                    this.password.get(),
                )
            }

            return JedisPool(this.host, this.port)
        }
    }

    companion object {
        fun fromEnvironment(): Configuration {
            return Configuration(
                clusterName = requireNotNull(System.getenv("SHULKER_CLUSTER_NAME")) { "Missing SHULKER_CLUSTER_NAME" },
                owningFleetName = Optional.ofNullable(System.getenv("SHULKER_OWNING_FLEET_NAME")),
                redis = Redis(
                    host = requireNotNull(System.getenv("SHULKER_REDIS_HOST")) { "Missing SHULKER_REDIS_HOST" },
                    port = Optional.ofNullable(System.getenv("SHULKER_REDIS_PORT"))
                        .map { it.toInt() }
                        .getOrDefault(6379),
                    username = Optional.ofNullable(System.getenv("SHULKER_REDIS_USERNAME")),
                    password = Optional.ofNullable(System.getenv("SHULKER_REDIS_PASSWORD"))
                )
            )
        }
    }
}

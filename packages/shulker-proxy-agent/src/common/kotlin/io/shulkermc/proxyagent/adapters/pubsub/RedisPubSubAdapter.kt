package io.shulkermc.proxyagent.adapters.pubsub

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.UUID
import java.util.concurrent.Executors

class RedisPubSubAdapter(private val jedisPool: JedisPool) : PubSubAdapter {
    private val executor = Executors.newCachedThreadPool()

    fun destroy() {
        this.executor.shutdownNow()
    }

    override fun teleportPlayerOnServer(
        playerId: UUID,
        serverName: String,
    ) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish("shulker:teleport", "$playerId:$serverName")
        }
    }

    override fun onTeleportPlayerOnServer(callback: (playerId: UUID, serverName: String) -> Unit) {
        this.executor.submit {
            this.jedisPool.resource.use { jedis ->
                jedis.subscribe(
                    object : JedisPubSub() {
                        override fun onMessage(
                            channel: String,
                            message: String,
                        ) {
                            val (playerId, serverName) = message.split(":")
                            callback(UUID.fromString(playerId), serverName)
                        }
                    },
                    "shulker:teleport",
                )
            }
        }
    }

    override fun drainProxy(proxyName: String) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish("shulker:drain", proxyName)
        }
    }

    override fun onDrainProxy(callback: (proxyName: String) -> Unit) {
        this.executor.submit {
            this.jedisPool.resource.use { jedis ->
                jedis.subscribe(
                    object : JedisPubSub() {
                        override fun onMessage(
                            channel: String,
                            message: String,
                        ) {
                            callback(message)
                        }
                    },
                    "shulker:drain",
                )
            }
        }
    }

    override fun reconnectProxy(proxyName: String) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish("shulker:reconnect-proxy", proxyName)
        }
    }

    override fun onReconnectProxy(callback: (proxyName: String) -> Unit) {
        this.executor.submit {
            this.jedisPool.resource.use { jedis ->
                jedis.subscribe(
                    object : JedisPubSub() {
                        override fun onMessage(
                            channel: String,
                            message: String,
                        ) {
                            callback(message)
                        }
                    },
                    "shulker:reconnect-proxy",
                )
            }
        }
    }
}

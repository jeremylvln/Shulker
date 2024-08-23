package io.shulkermc.proxyagent.adapters.pubsub

import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.util.concurrent.Executors

class RedisPubSubAdapter(private val jedisPool: JedisPool) : PubSubAdapter, AutoCloseable {
    private val executor = Executors.newCachedThreadPool()

    override fun close() {
        this.executor.shutdownNow()
    }

    override fun teleportPlayerOnServer(
        playerId: String,
        serverName: String,
    ) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish("shulker:teleport", "$playerId:$serverName")
        }
    }

    override fun onTeleportPlayerOnServer(callback: (playerId: String, serverName: String) -> Unit) {
        this.executor.submit {
            this.jedisPool.resource.use { jedis ->
                jedis.subscribe(
                    object : JedisPubSub() {
                        override fun onMessage(
                            channel: String,
                            message: String,
                        ) {
                            val (playerId, serverName) = message.split(":")
                            callback(playerId, serverName)
                        }
                    },
                    "shulker:teleport",
                )
            }
        }
    }
}

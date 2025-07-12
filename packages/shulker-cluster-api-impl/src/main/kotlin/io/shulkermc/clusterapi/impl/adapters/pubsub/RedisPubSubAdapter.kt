package io.shulkermc.clusterapi.impl.adapters.pubsub

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.io.Closeable
import java.util.Base64
import java.util.UUID
import java.util.concurrent.Executors
import java.util.function.Consumer

class RedisPubSubAdapter(private val instanceIdentity: String, private val jedisPool: JedisPool) : PubSubAdapter, Closeable {
    private val executor = Executors.newCachedThreadPool()

    override fun close() {
        this.executor.shutdownNow()
    }

    override fun subscribe(channel: String, callback: Consumer<String>) {
        this.executor.submit {
            this.jedisPool.resource.use { jedis ->
                jedis.subscribe(
                    object : JedisPubSub() {
                        override fun onMessage(
                            channel: String,
                            message: String,
                        ) {
                            callback.accept(message)
                        }
                    },
                    channel,
                    "${channel}@${this.instanceIdentity}"
                )
            }
        }
    }

    override fun broadcastToAllProxies(channel: String, message: String) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish(channel, message)
        }
    }

    override fun sendToProxy(proxyName: String, channel: String, message: String) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish("${channel}@${proxyName}", message)
        }
    }

    override fun broadcastToAllServers(channel: String, message: String) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish(channel, message)
        }
    }

    override fun sendToServer(serverName: String, channel: String, message: String) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish("${channel}@${serverName}", message)
        }
    }

    override fun teleportPlayerOnServer(
        playerId: UUID,
        serverName: String,
    ) {
        this.broadcastToAllProxies("shulker:teleport", "$playerId:$serverName")
    }

    override fun onTeleportPlayerOnServer(callback: (playerId: UUID, serverName: String) -> Unit) {
        this.subscribe("shulker:teleport") { message ->
            val (playerId, serverName) = message.split(":")
            callback(UUID.fromString(playerId), serverName)
        }
    }

    override fun disconnectPlayerFromCluster(playerId: UUID, message: Component) {
        val encodedMessage = Base64.getEncoder().encodeToString(JSONComponentSerializer.json().serialize(message).toByteArray())
        this.broadcastToAllProxies("shulker:kick", "$playerId:$encodedMessage")
    }

    override fun onDisconnectPlayerFromCluster(callback: (UUID, Component) -> Unit) {
        this.subscribe("shulker:kick") { message ->
            val (playerId, encodedMessage) = message.split(":")
            val decodedMessage = JSONComponentSerializer.json().deserialize(Base64.getDecoder().decode(encodedMessage).toString())
            callback(UUID.fromString(playerId), decodedMessage)
        }
    }

    override fun reconnectPlayerToCluster(playerId: UUID) {
        this.broadcastToAllProxies("shulker:reconnect", playerId.toString())
    }

    override fun onReconnectPlayerToCluster(callback: (UUID) -> Unit) {
        this.subscribe("shulker:reconnect") { playerId ->
            callback(UUID.fromString(playerId))
        }
    }

    override fun drainProxy(proxyName: String) {
        this.sendToProxy(proxyName, "shulker:drain", proxyName)
    }

    override fun onDrainProxy(callback: (proxyName: String) -> Unit) {
        this.subscribe("shulker:drain") { proxyName ->
            callback(proxyName)
        }
    }
}

package io.shulkermc.cluster.api.adapters.pubsub

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
    companion object {
        private const val CHANNEL_PREFIX = "shulker"
        private const val TELEPORT_CHANNEL = "$CHANNEL_PREFIX:teleport"
        private const val KICK_CHANNEL = "$CHANNEL_PREFIX:kick"
        private const val RECONNECT_CHANNEL = "$CHANNEL_PREFIX:reconnect"
        private const val DRAIN_CHANNEL = "$CHANNEL_PREFIX:drain"
    }

    private val executor = Executors.newCachedThreadPool()

    override fun close() {
        this.executor.shutdownNow()
    }

    override fun subscribe(
        channel: String,
        callback: Consumer<String>,
    ) {
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
                    "$channel@${this.instanceIdentity}",
                )
            }
        }
    }

    override fun broadcastToAllProxies(
        channel: String,
        message: String,
    ) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish(channel, message)
        }
    }

    override fun sendToProxy(
        proxyName: String,
        channel: String,
        message: String,
    ) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish("$channel@$proxyName", message)
        }
    }

    override fun broadcastToAllServers(
        channel: String,
        message: String,
    ) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish(channel, message)
        }
    }

    override fun sendToServer(
        serverName: String,
        channel: String,
        message: String,
    ) {
        this.jedisPool.resource.use { jedis ->
            jedis.publish("$channel@$serverName", message)
        }
    }

    override fun teleportPlayerOnServer(
        playerId: UUID,
        serverName: String,
    ) {
        this.broadcastToAllProxies(TELEPORT_CHANNEL, "$playerId:$serverName")
    }

    override fun onTeleportPlayerOnServer(callback: (playerId: UUID, serverName: String) -> Unit) {
        this.subscribe(TELEPORT_CHANNEL) { message ->
            val (playerId, serverName) = message.split(":")
            callback(UUID.fromString(playerId), serverName)
        }
    }

    override fun disconnectPlayerFromCluster(
        playerId: UUID,
        message: Component,
    ) {
        val encodedMessage = Base64.getEncoder().encodeToString(JSONComponentSerializer.json().serialize(message).toByteArray())
        this.broadcastToAllProxies(KICK_CHANNEL, "$playerId:$encodedMessage")
    }

    override fun onDisconnectPlayerFromCluster(callback: (UUID, Component) -> Unit) {
        this.subscribe(KICK_CHANNEL) { message ->
            val (playerId, encodedMessage) = message.split(":")
            val decodedMessage = JSONComponentSerializer.json().deserialize(Base64.getDecoder().decode(encodedMessage).toString())
            callback(UUID.fromString(playerId), decodedMessage)
        }
    }

    override fun reconnectPlayerToCluster(playerId: UUID) {
        this.broadcastToAllProxies(RECONNECT_CHANNEL, playerId.toString())
    }

    override fun onReconnectPlayerToCluster(callback: (UUID) -> Unit) {
        this.subscribe(RECONNECT_CHANNEL) { playerId ->
            callback(UUID.fromString(playerId))
        }
    }

    override fun drainProxy(proxyName: String) {
        this.sendToProxy(proxyName, DRAIN_CHANNEL, proxyName)
    }

    override fun onDrainProxy(callback: (proxyName: String) -> Unit) {
        this.subscribe(DRAIN_CHANNEL) { proxyName ->
            callback(proxyName)
        }
    }
}

package io.shulkermc.clusterapi.impl.adapters

import io.shulkermc.cluster.ShulkerClusterAPI
import io.shulkermc.cluster.data.PlayerPosition
import io.shulkermc.clusterapi.impl.adapters.cache.CacheAdapter
import io.shulkermc.clusterapi.impl.adapters.cache.RedisCacheAdapter
import io.shulkermc.clusterapi.impl.adapters.mojang.HttpMojangGatewayAdapter
import io.shulkermc.clusterapi.impl.adapters.mojang.MojangGatewayAdapter
import io.shulkermc.clusterapi.impl.adapters.pubsub.RedisPubSubAdapter
import io.shulkermc.cluster.messaging.MessagingBus
import net.kyori.adventure.text.Component
import redis.clients.jedis.JedisPool
import java.io.Closeable
import java.util.Optional
import java.util.UUID

class ShulkerClusterAPIImpl(val instanceIdentity: String) : ShulkerClusterAPI(), Closeable {
    private var jedisPool: JedisPool

    var mojangGateway: MojangGatewayAdapter
    var cache: CacheAdapter
    var pubSub: RedisPubSubAdapter

    init {
        INSTANCE = this

        this.jedisPool = this.createJedisPool()
        this.jedisPool.resource.use { jedis -> jedis.ping() }

        this.mojangGateway = HttpMojangGatewayAdapter()
        this.cache = RedisCacheAdapter(this.jedisPool)
        this.pubSub = RedisPubSubAdapter(this.instanceIdentity, this.jedisPool)
    }

    override fun close() {
        this.pubSub.close()
        this.jedisPool.destroy()
    }

    override fun getMessagingBus(): MessagingBus = this.pubSub

    override fun teleportPlayerOnServer(playerId: UUID, serverName: String) = this.pubSub.teleportPlayerOnServer(playerId, serverName)
    override fun disconnectPlayerFromCluster(playerId: UUID, message: Component) = this.pubSub.disconnectPlayerFromCluster(playerId, message)
    override fun reconnectPlayerToCluster(playerId: UUID) = this.pubSub.reconnectPlayerToCluster(playerId)
    override fun getPlayerPosition(playerId: UUID): Optional<PlayerPosition> = this.cache.getPlayerPosition(playerId)
    override fun isPlayerConnected(playerId: UUID): Boolean = this.cache.isPlayerConnected(playerId)
    override fun countOnlinePlayers(): Int = this.cache.countOnlinePlayers()

    override fun getPlayerIdFromName(playerName: String): Optional<UUID> {
        val cachedValue = this.cache.getPlayerIdFromName(playerName)
        if (cachedValue.isPresent) return cachedValue

        val mojangProfile = this.mojangGateway.getProfileFromName(playerName)
        if (mojangProfile.isPresent) {
            val playerId = mojangProfile.get().playerId
            this.cache.updateCachedPlayerName(playerId, playerName)
            return Optional.of(playerId)
        }

        return Optional.empty()
    }

    override fun getPlayerNameFromId(playerId: UUID): Optional<String> {
        val cachedValue = this.cache.getPlayerNameFromId(playerId)
        if (cachedValue.isPresent) return cachedValue

        val mojangProfile = this.mojangGateway.getProfileFromId(playerId)
        if (mojangProfile.isPresent) {
            val playerName = mojangProfile.get().playerName
            this.cache.updateCachedPlayerName(playerId, playerName)
            return Optional.of(playerName)
        }

        return Optional.empty()
    }

    private fun createJedisPool(): JedisPool {
        if (Configuration.REDIS_USERNAME.isPresent && Configuration.REDIS_PASSWORD.isPresent) {
            return JedisPool(
                Configuration.REDIS_HOST,
                Configuration.REDIS_PORT,
                Configuration.REDIS_USERNAME.get(),
                Configuration.REDIS_PASSWORD.get(),
            )
        }

        return JedisPool(Configuration.REDIS_HOST, Configuration.REDIS_PORT)
    }
}

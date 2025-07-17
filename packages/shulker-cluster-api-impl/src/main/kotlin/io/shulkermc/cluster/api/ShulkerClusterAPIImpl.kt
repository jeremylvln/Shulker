package io.shulkermc.cluster.api

import com.agones.dev.sdk.AgonesSDK
import com.agones.dev.sdk.AgonesSDKImpl
import com.agones.dev.sdk.GameServer
import io.shulkermc.cluster.api.adapters.cache.CacheAdapter
import io.shulkermc.cluster.api.adapters.cache.RedisCacheAdapter
import io.shulkermc.cluster.api.adapters.kubernetes.ImplKubernetesGatewayAdapter
import io.shulkermc.cluster.api.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.cluster.api.adapters.kubernetes.utils.objectRefFromFleetName
import io.shulkermc.cluster.api.adapters.kubernetes.utils.objectRefFromGameServer
import io.shulkermc.cluster.api.adapters.mojang.HttpMojangGatewayAdapter
import io.shulkermc.cluster.api.adapters.mojang.MojangGatewayAdapter
import io.shulkermc.cluster.api.adapters.pubsub.RedisPubSubAdapter
import io.shulkermc.cluster.api.data.KubernetesObjectRef
import io.shulkermc.cluster.api.data.PlayerPosition
import io.shulkermc.cluster.api.messaging.MessagingBus
import io.shulkermc.sdk.ShulkerSDK
import io.shulkermc.sdk.ShulkerSDKImpl
import net.kyori.adventure.text.Component
import redis.clients.jedis.JedisPool
import java.io.Closeable
import java.lang.Exception
import java.util.Optional
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

class ShulkerClusterAPIImpl(val logger: Logger) : ShulkerClusterAPI(), Closeable {
    private val configuration = Configuration.fromEnvironment()

    val agonesGateway: AgonesSDK

    val selfGameServer: GameServer
    val selfReference: KubernetesObjectRef
    val owningFleetReference: Optional<KubernetesObjectRef>

    val kubernetesGateway: KubernetesGatewayAdapter
    val jedisPool: JedisPool
    val mojangGateway: MojangGatewayAdapter
    val cache: CacheAdapter
    val pubSub: RedisPubSubAdapter

    var operatorSdk: ShulkerSDK? = null

    init {
        this.logger.fine("Creating Agones SDK from environment")
        this.agonesGateway = AgonesSDKImpl.createFromEnvironment()

        this.selfGameServer = this.agonesGateway.getGameServer().get()
        this.selfReference = objectRefFromGameServer(this.selfGameServer)
        this.owningFleetReference =
            this.configuration.owningFleetName
                .map { objectRefFromFleetName(this.selfGameServer.objectMeta.namespace, it) }

        this.logger.info("Identified game server: ${this.selfReference}")
        if (this.owningFleetReference.isPresent) {
            this.logger.info("Identified owning fleet: ${this.owningFleetReference.get()}")
        }

        this.kubernetesGateway = ImplKubernetesGatewayAdapter(this.selfReference, this.owningFleetReference)
        this.jedisPool = this.configuration.redis.createJedisPool()
        this.jedisPool.resource.use { jedis -> jedis.ping() }
        this.mojangGateway = HttpMojangGatewayAdapter()
        this.cache = RedisCacheAdapter(this.jedisPool)
        this.pubSub = RedisPubSubAdapter(this.selfReference.name, this.jedisPool)
    }

    override fun close() {
        try {
            this.pubSub.close()
            this.jedisPool.destroy()
            this.kubernetesGateway.destroy()
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            this.logger.log(Level.SEVERE, "Failed to properly destroy adapters", e)
        }

        try {
            this.agonesGateway.askShutdown()
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            this.logger.log(
                Level.SEVERE,
                "Failed to ask Agones sidecar to shutdown properly, stopping process manually",
                e,
            )

            exitProcess(0)
        }
    }

    override fun operator(): ShulkerSDK {
        this.operatorSdk = this.operatorSdk ?: ShulkerSDKImpl.createFromEnvironment()
        return this.operatorSdk!!
    }

    override fun messaging(): MessagingBus = this.pubSub

    override fun teleportPlayerOnServer(
        playerId: UUID,
        serverName: String,
    ) = this.pubSub.teleportPlayerOnServer(playerId, serverName)

    override fun disconnectPlayerFromCluster(
        playerId: UUID,
        message: Component,
    ) = this.pubSub.disconnectPlayerFromCluster(playerId, message)

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
}

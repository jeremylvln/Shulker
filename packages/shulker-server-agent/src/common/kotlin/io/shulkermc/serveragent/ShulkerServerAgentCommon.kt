package io.shulkermc.serveragent

import com.agones.dev.sdk.AgonesSDK
import com.agones.dev.sdk.AgonesSDKImpl
import io.shulkermc.agent.adapters.cache.CacheAdapter
import io.shulkermc.agent.adapters.cache.RedisCacheAdapter
import io.shulkermc.agent.adapters.kubernetes.ImplKubernetesGatewayAdapter
import io.shulkermc.agent.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.agent.adapters.mojang.HttpMojangGatewayAdapter
import io.shulkermc.agent.adapters.mojang.MojangGatewayAdapter
import io.shulkermc.agent.adapters.pubsub.PubSubAdapter
import io.shulkermc.agent.adapters.pubsub.RedisPubSubAdapter
import io.shulkermc.agent.api.ShulkerAPIHandler
import io.shulkermc.agent.services.ServerDirectoryService
import io.shulkermc.serveragent.api.ShulkerServerAPI
import io.shulkermc.serveragent.api.ShulkerServerAPIImpl
import io.shulkermc.serveragent.services.PlayerMovementService
import io.shulkermc.serveragent.tasks.HealthcheckTask
import redis.clients.jedis.JedisPool
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

class ShulkerServerAgentCommon(val serverInterface: ServerInterface, val logger: Logger) {
    companion object {
        private const val SUMMON_LABEL_NAME = "shulkermc.io/summoned"
        private const val SUMMON_TIMEOUT_MINUTES = 5L
    }

    private lateinit var jedisPool: JedisPool

    lateinit var kubernetesGateway: KubernetesGatewayAdapter
    lateinit var mojangGateway: MojangGatewayAdapter
    lateinit var cache: CacheAdapter
    lateinit var pubSub: PubSubAdapter
    lateinit var agonesGateway: AgonesSDK

    // Services
    lateinit var serverDirectoryService: ServerDirectoryService
    lateinit var playerMovementService: PlayerMovementService

    // Tasks
    private lateinit var healthcheckTask: ServerInterface.ScheduledTask
    private var summonTimeoutTask: ServerInterface.ScheduledTask? = null

    fun onServerInitialization() {
        try {
            this.logger.fine("Creating Agones SDK from environment")
            this.agonesGateway = AgonesSDKImpl.createFromEnvironment()
            val gameServer = this.agonesGateway.getGameServer().get()
            this.logger.info(
                "Identified Shulker server: ${gameServer.objectMeta.namespace}/${gameServer.objectMeta.name}",
            )

            this.logger.fine("Creating Redis pool")
            this.jedisPool = this.createJedisPool()
            this.jedisPool.resource.use { jedis -> jedis.ping() }

            this.kubernetesGateway = ImplKubernetesGatewayAdapter(Configuration.SERVER_NAMESPACE)
            this.mojangGateway = HttpMojangGatewayAdapter()
            this.cache = RedisCacheAdapter(this.jedisPool)
            this.pubSub = RedisPubSubAdapter(this.jedisPool)

            ShulkerServerAPI.INSTANCE = ShulkerServerAPIImpl(this, ShulkerAPIHandler(this.cache, this.mojangGateway))

            this.playerMovementService = PlayerMovementService(this)

            this.healthcheckTask = HealthcheckTask(this).schedule()

            if (Configuration.NETWORK_ADMINS.isNotEmpty()) {
                this.serverInterface.prepareNetworkAdminsPermissions(Configuration.NETWORK_ADMINS)
                this.logger.info(
                    "Created listener for ${Configuration.NETWORK_ADMINS.size} network administrators",
                )
            }

            if (gameServer.objectMeta.containsLabels(SUMMON_LABEL_NAME)) {
                this.logger.info(
                    "This server was summoned manually, it will be shutdown automatically in $SUMMON_TIMEOUT_MINUTES minutes",
                )
                this.summonTimeoutTask = this.createSummonTimeoutTask()
            }

            this.agonesGateway.setReady()
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            this.logger.log(Level.SEVERE, "Shulker Agent crashed, stopping server", e)
            this.shutdown()
        }
    }

    fun onServerShutdown() {
        this.shutdown()
    }

    fun shutdown() {
        try {
            this.summonTimeoutTask?.cancel()

            if (this::healthcheckTask.isInitialized) {
                this.healthcheckTask.cancel()
            }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            this.logger.log(Level.SEVERE, "Failed to properly terminate services", e)
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

    private fun createSummonTimeoutTask() =
        this.serverInterface.scheduleDelayedTask(
            SUMMON_TIMEOUT_MINUTES,
            TimeUnit.MINUTES,
        ) {
            this.agonesGateway.getState().thenAccept { state ->
                if (state == "Ready") {
                    this.logger.info(
                        "Server still in Ready state after $SUMMON_TIMEOUT_MINUTES minutes, asking shutdown",
                    )
                    this.agonesGateway.askShutdown()
                }
            }
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

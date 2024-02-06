package io.shulkermc.proxyagent

import com.agones.dev.sdk.AgonesSDK
import com.agones.dev.sdk.AgonesSDKImpl
import io.shulkermc.proxyagent.adapters.cache.CacheAdapter
import io.shulkermc.proxyagent.adapters.cache.RedisCacheAdapter
import io.shulkermc.proxyagent.adapters.filesystem.FileSystemAdapter
import io.shulkermc.proxyagent.adapters.filesystem.LocalFileSystemAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.ImplKubernetesGatewayAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.proxyagent.adapters.mojang.HttpMojangGatewayAdapter
import io.shulkermc.proxyagent.adapters.mojang.MojangGatewayAdapter
import io.shulkermc.proxyagent.adapters.pubsub.RedisPubSubAdapter
import io.shulkermc.proxyagent.api.ShulkerProxyAPI
import io.shulkermc.proxyagent.api.ShulkerProxyAPIImpl
import io.shulkermc.proxyagent.handlers.TeleportPlayerOnServerHandler
import io.shulkermc.proxyagent.services.PlayerMovementService
import io.shulkermc.proxyagent.services.ProxyLifecycleService
import io.shulkermc.proxyagent.services.ServerDirectoryService
import io.shulkermc.proxyagent.tasks.HealthcheckTask
import io.shulkermc.proxyagent.tasks.LostProxyPurgeTask
import redis.clients.jedis.JedisPool
import java.lang.Exception
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

class ShulkerProxyAgentCommon(val proxyInterface: ProxyInterface, val logger: Logger) {
    lateinit var agonesGateway: AgonesSDK
    private lateinit var jedisPool: JedisPool

    // Adapters
    lateinit var kubernetesGateway: KubernetesGatewayAdapter
    lateinit var fileSystem: FileSystemAdapter
    lateinit var mojangGateway: MojangGatewayAdapter
    lateinit var cache: CacheAdapter
    lateinit var pubSub: RedisPubSubAdapter

    // Services
    lateinit var serverDirectoryService: ServerDirectoryService
    lateinit var playerMovementService: PlayerMovementService
    private lateinit var proxyLifecycleService: ProxyLifecycleService

    // Tasks
    private lateinit var healthcheckTask: ProxyInterface.ScheduledTask
    private lateinit var lostProxyPurgeTask: ProxyInterface.ScheduledTask

    fun onProxyInitialization() {
        try {
            this.logger.fine("Creating Agones SDK from environment")
            this.agonesGateway = AgonesSDKImpl.createFromEnvironment()

            val gameServer = this.agonesGateway.getGameServer().get()
            this.logger.info(
                "Identified Shulker proxy: ${gameServer.objectMeta.namespace}/${gameServer.objectMeta.name}"
            )

            ShulkerProxyAPI.INSTANCE = ShulkerProxyAPIImpl(this)

            this.logger.fine("Creating Redis pool")
            this.jedisPool = this.createJedisPool()
            this.jedisPool.resource.use { jedis -> jedis.ping() }

            this.kubernetesGateway = ImplKubernetesGatewayAdapter(
                Configuration.PROXY_NAMESPACE,
                Configuration.PROXY_NAME
            )
            this.fileSystem = LocalFileSystemAdapter()
            this.mojangGateway = HttpMojangGatewayAdapter()
            this.cache = RedisCacheAdapter(this.jedisPool)
            this.pubSub = RedisPubSubAdapter(this.jedisPool)

            this.serverDirectoryService = ServerDirectoryService(this)
            this.playerMovementService = PlayerMovementService(this)
            this.proxyLifecycleService = ProxyLifecycleService(this)

            this.pubSub.onTeleportPlayerOnServer(TeleportPlayerOnServerHandler(this)::handle)

            this.healthcheckTask = HealthcheckTask(this).schedule()
            this.lostProxyPurgeTask = LostProxyPurgeTask(this).schedule()

            if (Configuration.NETWORK_ADMINS.isNotEmpty()) {
                this.proxyInterface.prepareNetworkAdminsPermissions(Configuration.NETWORK_ADMINS)
                this.logger.info(
                    "Created listener for ${Configuration.NETWORK_ADMINS.size} network administrators"
                )
            }

            this.cache.registerProxy(Configuration.PROXY_NAME, this.proxyInterface.getPlayerCapacity())
            this.agonesGateway.setReady()
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            this.logger.log(Level.SEVERE, "Shulker Agent crashed, stopping proxy", e)
            this.shutdown()
        }
    }

    fun onProxyShutdown() {
        this.cache.unregisterProxy(Configuration.PROXY_NAME)

        this.healthcheckTask.cancel()
        this.proxyLifecycleService.destroy()
        this.kubernetesGateway.destroy()
        this.jedisPool.destroy()
        this.agonesGateway.askShutdown()
        this.agonesGateway.destroy()
    }

    fun shutdown() {
        try {
            this.cache.unregisterProxy(Configuration.PROXY_NAME)
            this.pubSub.close()
            this.agonesGateway.askShutdown()
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            this.logger.log(
                Level.SEVERE,
                "Failed to ask Agones sidecar to shutdown properly, stopping process manually",
                e
            )
            exitProcess(0)
        }
    }

    private fun createJedisPool(): JedisPool {
        if (Configuration.REDIS_USERNAME.isPresent && Configuration.REDIS_PASSWORD.isPresent) {
            return JedisPool(
                Configuration.REDIS_HOST,
                Configuration.REDIS_PORT,
                Configuration.REDIS_USERNAME.get(),
                Configuration.REDIS_PASSWORD.get()
            )
        }

        return JedisPool(Configuration.REDIS_HOST, Configuration.REDIS_PORT)
    }
}

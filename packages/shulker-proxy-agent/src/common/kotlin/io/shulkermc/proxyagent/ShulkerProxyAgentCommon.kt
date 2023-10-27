package io.shulkermc.proxyagent

import dev.agones.AgonesSDK
import dev.agones.AgonesSDKImpl
import io.shulkermc.proxyagent.adapters.cache.CacheAdapter
import io.shulkermc.proxyagent.adapters.cache.RedisCacheAdapter
import io.shulkermc.proxyagent.adapters.filesystem.FileSystemAdapter
import io.shulkermc.proxyagent.adapters.filesystem.LocalFileSystemAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.ImplKubernetesGatewayAdapter
import io.shulkermc.proxyagent.api.ShulkerProxyAPI
import io.shulkermc.proxyagent.api.ShulkerProxyAPIImpl
import io.shulkermc.proxyagent.services.PlayerMovementService
import io.shulkermc.proxyagent.services.ProxyLifecycleService
import io.shulkermc.proxyagent.services.ServerDirectoryService
import io.shulkermc.proxyagent.tasks.HealthcheckTask
import io.shulkermc.proxyagent.tasks.LostProxyPurgeTask
import redis.clients.jedis.JedisPool
import java.lang.Exception
import java.util.logging.Logger
import kotlin.system.exitProcess

class ShulkerProxyAgentCommon(val proxyInterface: ProxyInterface, val logger: Logger) {
    lateinit var agonesGateway: AgonesSDK
    private lateinit var jedisPool: JedisPool

    // Adapters
    lateinit var kubernetesGateway: KubernetesGatewayAdapter
    lateinit var fileSystem: FileSystemAdapter
    lateinit var cache: CacheAdapter

    // Services
    lateinit var serverDirectoryService: ServerDirectoryService
    lateinit var playerMovementService: PlayerMovementService
    private lateinit var proxyLifecycleService: ProxyLifecycleService

    private lateinit var healthcheckTask: ProxyInterface.ScheduledTask
    private lateinit var lostProxyPurgeTask: ProxyInterface.ScheduledTask

    fun onProxyInitialization() {
        try {
            this.agonesGateway = AgonesSDKImpl.createFromEnvironment()
            val gameServer = this.agonesGateway.getGameServer().get()
            this.logger.info("Identified Shulker proxy: ${gameServer.objectMeta.namespace}/${gameServer.objectMeta.name}")

            ShulkerProxyAPI.INSTANCE = ShulkerProxyAPIImpl(this)

            this.jedisPool = this.createJedisPool()
            this.jedisPool.resource.use { jedis -> jedis.ping() }

            this.fileSystem = LocalFileSystemAdapter()
            this.kubernetesGateway = ImplKubernetesGatewayAdapter(Configuration.PROXY_NAMESPACE, Configuration.PROXY_NAME)
            this.cache = RedisCacheAdapter(this.jedisPool)

            this.serverDirectoryService = ServerDirectoryService(this)
            this.playerMovementService = PlayerMovementService(this)
            this.proxyLifecycleService = ProxyLifecycleService(this)

            this.healthcheckTask = HealthcheckTask(this).schedule()
            this.lostProxyPurgeTask = LostProxyPurgeTask(this).schedule()

            this.cache.registerProxy(Configuration.PROXY_NAME)
            this.agonesGateway.setAllocated()
        } catch (e: Exception) {
            this.logger.severe("Failed to parse configuration")
            e.printStackTrace()
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
            this.agonesGateway.askShutdown()
        } catch (ex: Exception) {
            this.logger.severe("Failed to ask Agones sidecar to shutdown properly, stopping process manually")
            exitProcess(0)
        }
    }

    private fun createJedisPool(): JedisPool {
        if (Configuration.REDIS_USERNAME != null && Configuration.REDIS_PASSWORD != null)
            return JedisPool(Configuration.REDIS_HOST, Configuration.REDIS_PORT)
        return JedisPool(Configuration.REDIS_HOST, Configuration.REDIS_PORT, Configuration.REDIS_USERNAME, Configuration.REDIS_PASSWORD)
    }
}

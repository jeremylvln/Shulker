package io.shulkermc.proxy

import io.shulkermc.cluster.api.ShulkerClusterAPIImpl
import io.shulkermc.proxy.adapters.filesystem.FileSystemAdapter
import io.shulkermc.proxy.adapters.filesystem.LocalFileSystemAdapter
import io.shulkermc.proxy.api.ShulkerProxyAPIImpl
import io.shulkermc.proxy.handlers.DisconnectPlayerFromClusterHandler
import io.shulkermc.proxy.handlers.DrainProxyHandler
import io.shulkermc.proxy.handlers.ReconnectPlayerOnProxyHandler
import io.shulkermc.proxy.handlers.TeleportPlayerOnServerHandler
import io.shulkermc.proxy.services.PlayerMovementService
import io.shulkermc.proxy.services.ProxyLifecycleService
import io.shulkermc.proxy.services.ServerDirectoryService
import io.shulkermc.proxy.tasks.HealthcheckTask
import io.shulkermc.proxy.tasks.LostProxyPurgeTask
import java.lang.Exception
import java.util.logging.Level
import java.util.logging.Logger

class ShulkerProxyAgentCommon(val proxyInterface: ProxyInterface, val logger: Logger) {
    lateinit var cluster: ShulkerClusterAPIImpl
    lateinit var api: ShulkerProxyAPIImpl

    // Adapters
    lateinit var fileSystem: FileSystemAdapter

    // Services
    lateinit var serverDirectoryService: ServerDirectoryService
    lateinit var playerMovementService: PlayerMovementService
    lateinit var proxyLifecycleService: ProxyLifecycleService

    // Tasks
    private lateinit var healthcheckTask: ProxyInterface.ScheduledTask
    private lateinit var lostProxyPurgeTask: ProxyInterface.ScheduledTask

    fun onProxyInitialization() {
        this.logger.info("Agent version ${BuildConfig.VERSION} built on ${BuildConfig.BUILD_TIME}")

        try {
            this.cluster = ShulkerClusterAPIImpl(this.logger)
            this.api = ShulkerProxyAPIImpl(this)

            this.fileSystem = LocalFileSystemAdapter()

            this.serverDirectoryService = ServerDirectoryService(this)
            this.playerMovementService = PlayerMovementService(this)
            this.proxyLifecycleService = ProxyLifecycleService(this)

            this.cluster.pubSub.onTeleportPlayerOnServer(TeleportPlayerOnServerHandler(this)::handle)
            this.cluster.pubSub.onDrainProxy(DrainProxyHandler(this)::handle)
            this.cluster.pubSub.onReconnectPlayerToCluster(ReconnectPlayerOnProxyHandler(this)::handle)
            this.cluster.pubSub.onDisconnectPlayerFromCluster(DisconnectPlayerFromClusterHandler(this)::handle)

            this.healthcheckTask = HealthcheckTask(this).schedule()
            this.lostProxyPurgeTask = LostProxyPurgeTask(this).schedule()

            if (Configuration.NETWORK_ADMINS.isNotEmpty()) {
                this.proxyInterface.prepareNetworkAdminsPermissions(Configuration.NETWORK_ADMINS)
                this.logger.info(
                    "Created listener for ${Configuration.NETWORK_ADMINS.size} network administrators",
                )
            }

            this.cluster.cache.registerProxy(this.cluster.selfReference.name, this.proxyInterface.getPlayerCapacity())
            this.cluster.agonesGateway.setReady()

            this.logger.info("Proxy is ready")
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            this.logger.log(Level.SEVERE, "Shulker Agent crashed, stopping proxy", e)
        }
    }

    fun onProxyShutdown() {
        this.shutdown()
    }

    fun shutdown() {
        this.cluster.cache.unregisterProxy(this.cluster.selfReference.name)

        try {
            this.logger.info("Trying to reconnect everyone to cluster")
            this.playerMovementService.reconnectEveryoneToCluster()
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            this.logger.log(Level.WARNING, "Failed to reconnect everyone to cluster, connected players will be disconnected", e)
        }

        try {
            if (this::healthcheckTask.isInitialized) {
                this.healthcheckTask.cancel()
            }

            if (this::proxyLifecycleService.isInitialized) {
                this.proxyLifecycleService.destroy()
            }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            this.logger.log(Level.SEVERE, "Failed to properly terminate services", e)
        }

        this.cluster.close()
    }
}

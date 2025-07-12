package io.shulkermc.proxyagent

import com.agones.dev.sdk.AgonesSDK
import com.agones.dev.sdk.AgonesSDKImpl
import io.shulkermc.clusterapi.impl.adapters.ShulkerClusterAPIImpl
import io.shulkermc.proxyagent.adapters.filesystem.FileSystemAdapter
import io.shulkermc.proxyagent.adapters.filesystem.LocalFileSystemAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.ImplKubernetesGatewayAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.proxyagent.api.ShulkerProxyAPI
import io.shulkermc.proxyagent.api.ShulkerProxyAPIImpl
import io.shulkermc.proxyagent.handlers.DrainProxyHandler
import io.shulkermc.proxyagent.handlers.ReconnectPlayerOnProxyHandler
import io.shulkermc.proxyagent.handlers.TeleportPlayerOnServerHandler
import io.shulkermc.proxyagent.services.PlayerMovementService
import io.shulkermc.proxyagent.services.ProxyLifecycleService
import io.shulkermc.proxyagent.services.ServerDirectoryService
import io.shulkermc.proxyagent.tasks.HealthcheckTask
import io.shulkermc.proxyagent.tasks.LostProxyPurgeTask
import java.lang.Exception
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.exitProcess

class ShulkerProxyAgentCommon(val proxyInterface: ProxyInterface, val logger: Logger) {
    lateinit var agonesGateway: AgonesSDK
    lateinit var cluster: ShulkerClusterAPIImpl

    // Adapters
    lateinit var kubernetesGateway: KubernetesGatewayAdapter
    lateinit var fileSystem: FileSystemAdapter

    // Services
    lateinit var serverDirectoryService: ServerDirectoryService
    lateinit var playerMovementService: PlayerMovementService
    lateinit var proxyLifecycleService: ProxyLifecycleService

    // Tasks
    private lateinit var healthcheckTask: ProxyInterface.ScheduledTask
    private lateinit var lostProxyPurgeTask: ProxyInterface.ScheduledTask

    fun onProxyInitialization() {
        try {
            this.logger.fine("Creating Agones SDK from environment")
            this.agonesGateway = AgonesSDKImpl.createFromEnvironment()

            val gameServer = this.agonesGateway.getGameServer().get()
            this.logger.info(
                "Identified Shulker proxy: ${gameServer.objectMeta.namespace}/${gameServer.objectMeta.name}",
            )

            this.cluster = ShulkerClusterAPIImpl(gameServer.objectMeta.name)
            ShulkerProxyAPI.INSTANCE = ShulkerProxyAPIImpl(this)

            this.kubernetesGateway =
                ImplKubernetesGatewayAdapter(
                    Configuration.PROXY_NAMESPACE,
                    Configuration.PROXY_NAME,
                )
            this.fileSystem = LocalFileSystemAdapter()

            this.serverDirectoryService = ServerDirectoryService(this)
            this.playerMovementService = PlayerMovementService(this)
            this.proxyLifecycleService = ProxyLifecycleService(this)

            this.cluster.pubSub.onTeleportPlayerOnServer(TeleportPlayerOnServerHandler(this)::handle)
            this.cluster.pubSub.onDrainProxy(DrainProxyHandler(this)::handle)
            this.cluster.pubSub.onReconnectPlayerToCluster(ReconnectPlayerOnProxyHandler(this)::handle)

            this.healthcheckTask = HealthcheckTask(this).schedule()
            this.lostProxyPurgeTask = LostProxyPurgeTask(this).schedule()

            if (Configuration.NETWORK_ADMINS.isNotEmpty()) {
                this.proxyInterface.prepareNetworkAdminsPermissions(Configuration.NETWORK_ADMINS)
                this.logger.info(
                    "Created listener for ${Configuration.NETWORK_ADMINS.size} network administrators",
                )
            }

            this.cluster.cache.registerProxy(Configuration.PROXY_NAME, this.proxyInterface.getPlayerCapacity())
            this.agonesGateway.setReady()

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
        this.cluster.cache.unregisterProxy(Configuration.PROXY_NAME)

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

            if (this::kubernetesGateway.isInitialized) {
                this.kubernetesGateway.destroy()
            }

            this.cluster.close()
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
}

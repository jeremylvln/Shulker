package io.shulkermc.proxyagent

import dev.agones.AgonesSDK
import dev.agones.AgonesSDKImpl
import io.shulkermc.proxyagent.adapters.filesystem.FileSystemAdapter
import io.shulkermc.proxyagent.adapters.filesystem.FileSystemAdapterImpl
import io.shulkermc.proxyagent.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.KubernetesGatewayAdapterImpl
import io.shulkermc.proxyagent.api.ShulkerProxyAPI
import io.shulkermc.proxyagent.api.ShulkerProxyAPIImpl
import io.shulkermc.proxyagent.services.PlayerMovementService
import io.shulkermc.proxyagent.services.ProxyLifecycleService
import io.shulkermc.proxyagent.services.ServerDirectoryService
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import kotlin.system.exitProcess

class ShulkerProxyAgentCommon(val proxyInterface: ProxyInterface, val logger: Logger) {
    private lateinit var agonesGateway: AgonesSDK
    lateinit var kubernetesGateway: KubernetesGatewayAdapter
    lateinit var fileSystem: FileSystemAdapter

    lateinit var serverDirectoryService: ServerDirectoryService
    lateinit var playerMovementService: PlayerMovementService
    private lateinit var proxyLifecycleService: ProxyLifecycleService

    private lateinit var healthcheckTask: ProxyInterface.ScheduledTask

    fun onProxyInitialization() {
        try {
            this.agonesGateway = AgonesSDKImpl.createFromEnvironment()
            val gameServer = this.agonesGateway.getGameServer().get()
            this.logger.info("Identified Shulker proxy: ${gameServer.objectMeta.namespace}/${gameServer.objectMeta.name}")

            ShulkerProxyAPI.INSTANCE = ShulkerProxyAPIImpl(this)

            this.fileSystem = FileSystemAdapterImpl()
            this.kubernetesGateway = KubernetesGatewayAdapterImpl(Configuration.PROXY_NAMESPACE, Configuration.PROXY_NAME)

            this.serverDirectoryService = ServerDirectoryService(this)
            this.playerMovementService = PlayerMovementService(this)
            this.proxyLifecycleService = ProxyLifecycleService(this)

            this.healthcheckTask = this.proxyInterface.scheduleRepeatingTask(0L, 5L, TimeUnit.SECONDS) {
                this.agonesGateway.sendHealthcheck()
            }

            this.agonesGateway.setAllocated()
        } catch (e: Exception) {
            this.logger.severe("Failed to parse configuration")
            e.printStackTrace()
            this.shutdown()
        }
    }

    fun onProxyShutdown() {
        this.healthcheckTask.cancel()
        this.proxyLifecycleService.destroy()
        this.kubernetesGateway.destroy()
        this.agonesGateway.askShutdown()
        this.agonesGateway.destroy()
    }

    fun shutdown() {
        try {
            this.agonesGateway.askShutdown()
        } catch (ex: Exception) {
            this.logger.severe("Failed to ask Agones sidecar to shutdown properly, stopping process manually")
            exitProcess(0)
        }
    }
}

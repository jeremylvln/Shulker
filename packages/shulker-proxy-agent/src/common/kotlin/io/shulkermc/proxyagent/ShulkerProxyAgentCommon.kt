package io.shulkermc.proxyagent

import agones.dev.sdk.AgonesSDK
import agones.dev.sdk.AgonesSDKImpl
import io.shulkermc.proxyagent.adapters.filesystem.FileSystemAdapterImpl
import io.shulkermc.proxyagent.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.KubernetesGatewayAdapterImpl
import io.shulkermc.proxyagent.api.ShulkerProxyAPIImpl
import io.shulkermc.proxyagent.features.directory.DirectoryFeature
import io.shulkermc.proxyagent.features.drain.DrainFeature
import io.shulkermc.proxyagent.features.limbo.LimboFeature
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class ShulkerProxyAgentCommon(val proxyInterface: ProxyInterface, val logger: Logger) {
    val api = ShulkerProxyAPIImpl(this)
    lateinit var agonesGateway: AgonesSDK
    private lateinit var kubernetesGateway: KubernetesGatewayAdapter
    private lateinit var drainFeature: DrainFeature
    private lateinit var healthcheckTask: ProxyInterface.ScheduledTask

    fun onProxyInitialization() {
        try {
            this.agonesGateway = AgonesSDKImpl.createFromEnvironment()
            val gameServer = this.agonesGateway.getGameServer().get()
            this.logger.info("Identified Shulker proxy: ${gameServer.objectMeta.namespace}/${gameServer.objectMeta.name}")

            val fileSystem = FileSystemAdapterImpl()
            this.kubernetesGateway = KubernetesGatewayAdapterImpl(Configuration.PROXY_NAMESPACE, Configuration.PROXY_NAME)

            this.drainFeature = DrainFeature(this, fileSystem, this.kubernetesGateway, Configuration.PROXY_TTL_SECONDS)
            DirectoryFeature(this, this.kubernetesGateway)
            LimboFeature(this)

            this.healthcheckTask = this.proxyInterface.scheduleRepeatingTask(0L, 5L, TimeUnit.SECONDS) {
                this.agonesGateway.sendHealthcheck()
            }

            this.agonesGateway.setAllocated()
        } catch (e: Exception) {
            this.logger.severe("Failed to parse configuration")
            e.printStackTrace()
            this.agonesGateway.askShutdown()
        }
    }

    fun onProxyShutdown() {
        this.healthcheckTask.cancel()
        this.drainFeature.destroy()
        this.kubernetesGateway.destroy()
        this.agonesGateway.askShutdown()
    }
}

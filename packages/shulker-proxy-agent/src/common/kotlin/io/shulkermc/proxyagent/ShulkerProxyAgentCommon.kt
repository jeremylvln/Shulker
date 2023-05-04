package io.shulkermc.proxyagent

import io.shulkermc.proxyagent.adapters.agones.AgonesGatewayAdapter
import io.shulkermc.proxyagent.adapters.agones.AgonesGatewayAdapterImpl
import io.shulkermc.proxyagent.adapters.filesystem.FileSystemAdapterImpl
import io.shulkermc.proxyagent.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.KubernetesGatewayAdapterImpl
import io.shulkermc.proxyagent.api.ShulkerProxyAPIImpl
import io.shulkermc.proxyagent.features.directory.DirectoryFeature
import io.shulkermc.proxyagent.features.drain.DrainFeature
import io.shulkermc.proxyagent.features.limbo.LimboFeature
import java.lang.Exception
import java.util.logging.Logger

class ShulkerProxyAgentCommon(val proxyInterface: ProxyInterface, val logger: Logger) {
    val api = ShulkerProxyAPIImpl(this)
    private lateinit var kubernetesGateway: KubernetesGatewayAdapter
    lateinit var agonesGateway: AgonesGatewayAdapter

    fun onProxyInitialization() {
        try {
            this.logger.info("Identified Shulker proxy: ${Configuration.PROXY_NAMESPACE}/${Configuration.PROXY_NAME}")

            val fileSystem = FileSystemAdapterImpl()
            this.kubernetesGateway = KubernetesGatewayAdapterImpl(Configuration.PROXY_NAMESPACE, Configuration.PROXY_NAME)
            this.agonesGateway = AgonesGatewayAdapterImpl()

            DrainFeature(this, fileSystem, this.kubernetesGateway, Configuration.PROXY_TTL_SECONDS)
            DirectoryFeature(this, this.kubernetesGateway)
            LimboFeature(this)

            this.agonesGateway.emitProxyReady()
        } catch (e: Exception) {
            this.logger.severe("Failed to parse configuration")
            e.printStackTrace()
        }
    }

    fun onProxyShutdown() {
        this.kubernetesGateway.destroy()
        this.agonesGateway.emitProxyShutdown()
    }
}

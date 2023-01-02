package io.shulkermc.proxyagent

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
    private var kubernetesGateway: KubernetesGatewayAdapter? = null

    fun onProxyInitialization() {
        try {
            val config = parse()

            this.logger.info("Identified Shulker proxy: ${config.proxyNamespace}/${config.proxyName}")

            val fileSystem = FileSystemAdapterImpl()
            this.kubernetesGateway = KubernetesGatewayAdapterImpl(config.proxyNamespace, config.proxyName)

            DrainFeature(this, fileSystem, kubernetesGateway!!, config.ttlSeconds)
            DirectoryFeature(this, kubernetesGateway!!)
            LimboFeature(this)

            kubernetesGateway!!.emitAgentReady()
        } catch (e: Exception) {
            this.logger.severe("Failed to parse configuration")
            e.printStackTrace()
            this.proxyInterface.shutdown()
        }
    }

    fun onProxyShutdown() {
        if (this.kubernetesGateway != null)
            this.kubernetesGateway!!.destroy()
    }
}

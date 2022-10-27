package io.shulkermc.proxyagent.features.directory

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.WatchAction
import io.shulkermc.proxyagent.adapters.kubernetes.models.ShulkerV1alpha1MinecraftServer
import java.net.InetSocketAddress
import java.util.*
import kotlin.collections.HashSet
import kotlin.jvm.optionals.getOrElse

@OptIn(ExperimentalStdlibApi::class)
class DirectoryFeature(
    private val agent: ShulkerProxyAgentCommon,
    kubernetesGateway: KubernetesGatewayAdapter,
) {
    init {
        kubernetesGateway.watchMinecraftServerEvent { action, minecraftServer ->
            agent.logger.fine("Detected modification on Kubernetes MinecraftServer '${minecraftServer.metadata.name}'")
            if (action == WatchAction.ADDED || action == WatchAction.MODIFIED)
                this.registerServer(minecraftServer)
            else if (action == WatchAction.DELETED)
                this.unregisterServer(minecraftServer)
        }

        val existingMinecraftServers = kubernetesGateway.listMinecraftServers()
        existingMinecraftServers.items
            .filterNotNull()
            .forEach { minecraftServer -> registerServer(minecraftServer) }
    }

    private fun registerServer(minecraftServer: ShulkerV1alpha1MinecraftServer) {
        val alreadyKnown = this.agent.proxyInterface.hasServer(minecraftServer.metadata.name)

        if (alreadyKnown || minecraftServer.status == null)
            return

        val readyCondition = minecraftServer.status.getConditionByType("Ready")
        val isReady = readyCondition.map { condition ->
            condition.status == "True"
        }.getOrElse { false }

        if (isReady) {
            this.agent.api.directoryAdapter.registerServer(
                minecraftServer.metadata.name,
                InetSocketAddress(minecraftServer.status.serverIP, 25565),
                if (minecraftServer.spec.tags != null) HashSet(minecraftServer.spec.tags!!) else HashSet()
            )
        }
    }

    private fun unregisterServer(minecraftServer: ShulkerV1alpha1MinecraftServer) {
        this.agent.api.directoryAdapter.unregisterServer(minecraftServer.metadata.name)
    }
}

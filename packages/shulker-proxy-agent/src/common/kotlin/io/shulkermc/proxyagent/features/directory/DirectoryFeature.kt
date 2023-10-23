package io.shulkermc.proxyagent.features.directory

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.WatchAction
import io.shulkermc.proxyagent.adapters.kubernetes.models.AgonesV1GameServer
import java.net.InetSocketAddress

class DirectoryFeature(
    private val agent: ShulkerProxyAgentCommon,
    kubernetesGateway: KubernetesGatewayAdapter,
) {
    init {
        kubernetesGateway.watchMinecraftServerEvents { action, minecraftServer ->
            this.agent.logger.fine("Detected modification on Minecraft Server '${minecraftServer.metadata.name}'")
            if (action == WatchAction.ADDED || action == WatchAction.MODIFIED)
                this.registerServer(minecraftServer)
            else if (action == WatchAction.DELETED)
                this.unregisterServer(minecraftServer)
        }

        val existingMinecraftServers = kubernetesGateway.listMinecraftServers()
        existingMinecraftServers.items
            .filterNotNull()
            .forEach { gameServer -> registerServer(gameServer) }
    }

    private fun registerServer(gameServer: AgonesV1GameServer) {
        val alreadyKnown = this.agent.proxyInterface.hasServer(gameServer.metadata.name)

        if (alreadyKnown || gameServer.status == null)
            return

        if (gameServer.status.isReady()) {
            val tags = gameServer.metadata.annotations["minecraftserver.shulkermc.io/tags"]
            this.agent.api.registerServer(
                gameServer.metadata.name,
                InetSocketAddress(gameServer.status.address, gameServer.status.ports!![0].port!!),
                tags?.split(",")?.toSet() ?: emptySet()
            )
        }
    }

    private fun unregisterServer(gameServer: AgonesV1GameServer) {
        this.agent.api.unregisterServer(gameServer.metadata.name)
    }
}

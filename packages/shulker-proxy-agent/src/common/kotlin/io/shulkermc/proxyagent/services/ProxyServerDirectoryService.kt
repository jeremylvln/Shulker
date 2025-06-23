package io.shulkermc.proxyagent.services
import io.shulkermc.agent.Configuration
import io.shulkermc.agent.adapters.kubernetes.models.AgonesV1GameServer
import io.shulkermc.agent.services.ServerDirectoryService
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.adapters.filesystem.FileSystemAdapter
import io.shulkermc.proxyagent.utils.addressFromHostString
import java.net.InetSocketAddress
import java.util.Optional

class ProxyServerDirectoryService(
    private val agent: ShulkerProxyAgentCommon
) : ServerDirectoryService(
    kubernetesGateway = agent.kubernetesGateway,
    cache = agent.cache,
    logger = agent.logger
) {

    private var externalServers: Optional<Map<String, FileSystemAdapter.ExternalServer>> = Optional.empty()

    init {
        this.agent.fileSystem.watchExternalServersUpdates(this::onExternalServersUpdate)
    }

    private fun onExternalServersUpdate(servers: Map<String, FileSystemAdapter.ExternalServer>) {
        this.agent.logger.info("External servers file was updated, updating directory")

        this.externalServers.ifPresent { existingServer ->
            existingServer.keys.forEach(this::unregisterServer)
        }

        this.externalServers = Optional.of(servers)

        servers.values.forEach { server ->
            this.registerServer(server.name, server.address, server.tags)
        }
    }

    override fun registerServer(minecraftServer: AgonesV1GameServer) {
        val alreadyKnown = this.agent.proxyInterface.hasServer(minecraftServer.metadata.name)
        if (alreadyKnown) {
            return
        }

        if (minecraftServer.status != null && minecraftServer.status.isReady()) {
            this.registerServer(
                minecraftServer.metadata.name,
                addressFromHostString(
                    "${minecraftServer.metadata.name}.${Configuration.CLUSTER_NAME}-cluster.${minecraftServer.metadata.namespace}",
                )
            )
        }

        super.registerServer(minecraftServer)
    }

    fun registerServer(
        name: String,
        address: InetSocketAddress,
        tags: Set<String>,
    ) {
        this.registerServer(name, tags)
        this.registerServer(name, address)
    }

    private fun registerServer(
        name: String,
        address: InetSocketAddress
    ) {
        this.agent.proxyInterface.registerServer(name, address);
    }

    override fun unregisterServer(name: String) {
        if (this.agent.proxyInterface.unregisterServer(name)) {
            super.unregisterServer(name)
        }
        this.agent.cache.unregisterServer(name)
        this.agent.logger.info("Removed server '$name' from directory")
    }
}

package io.shulkermc.proxyagent.services

import io.shulkermc.proxyagent.Configuration
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.adapters.filesystem.FileSystemAdapter
import io.shulkermc.proxyagent.adapters.kubernetes.WatchAction
import io.shulkermc.proxyagent.adapters.kubernetes.models.AgonesV1GameServer
import io.shulkermc.proxyagent.utils.addressFromHostString
import java.net.InetSocketAddress
import java.util.Optional

class ServerDirectoryService(
    private val agent: ShulkerProxyAgentCommon,
) {
    private val serversByTag = HashMap<String, MutableSet<String>>()
    private val tagsByServer = HashMap<String, Set<String>>()

    private var externalServers: Optional<Map<String, FileSystemAdapter.ExternalServer>> = Optional.empty()

    init {
        this.agent.kubernetesGateway.watchMinecraftServerEvents { action, minecraftServer ->
            this.agent.logger.fine("Detected modification on MinecraftServer '${minecraftServer.metadata.name}'")
            if (action == WatchAction.ADDED || action == WatchAction.MODIFIED) {
                this.registerServer(minecraftServer)
            } else if (action == WatchAction.DELETED) {
                this.unregisterServer(minecraftServer.metadata.name)
            }
        }

        val existingMinecraftServers = this.agent.kubernetesGateway.listMinecraftServers()
        existingMinecraftServers.items
            .filterNotNull()
            .forEach(this::registerServer)

        this.agent.fileSystem.watchExternalServersUpdates(this::onExternalServersUpdate)
    }

    fun getServersByTag(tag: String): Set<String> = this.serversByTag.getOrDefault(tag, setOf())

    private fun registerServer(minecraftServer: AgonesV1GameServer) {
        val alreadyKnown = this.agent.proxyInterface.hasServer(minecraftServer.metadata.name)

        if (alreadyKnown || minecraftServer.status == null) {
            return
        }

        if (minecraftServer.status.isReady()) {
            val tags = minecraftServer.metadata.annotations["minecraftserver.shulkermc.io/tags"]

            this.registerServer(
                minecraftServer.metadata.name,
                addressFromHostString(
                    "${minecraftServer.metadata.name}.${Configuration.CLUSTER_NAME}-cluster.${minecraftServer.metadata.namespace}",
                ),
                tags?.split(",")?.toSet().orEmpty(),
            )
        }
    }

    private fun registerServer(
        name: String,
        address: InetSocketAddress,
        tags: Set<String>,
    ) {
        this.agent.proxyInterface.registerServer(name, address)

        for (tag in tags) {
            this.serversByTag.getOrPut(tag) { mutableSetOf() }.add(name)
            this.agent.logger.fine("Tagged '$tag' on server '$name'")
        }
        this.tagsByServer[name] = tags
        this.agent.logger.info("Added server '$name' to directory")
    }

    private fun unregisterServer(name: String) {
        if (this.agent.proxyInterface.unregisterServer(name)) {
            val tags = this.tagsByServer[name]
            if (tags != null) {
                tags.forEach { tag -> this.serversByTag[tag]!!.remove(name) }
                this.tagsByServer.remove(name)
            }

            this.agent.cache.unregisterServer(name)
            this.agent.logger.info("Removed server '$name' from directory")
        }
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
}

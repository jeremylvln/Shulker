package io.shulkermc.proxyagent.services

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.adapters.kubernetes.WatchAction
import io.shulkermc.proxyagent.adapters.kubernetes.models.AgonesV1GameServer
import java.net.InetSocketAddress

class ServerDirectoryService(
    private val agent: ShulkerProxyAgentCommon
) {
    private val serversByTag = HashMap<String, MutableSet<String>>()
    private val tagsByServer = HashMap<String, Set<String>>()

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
                InetSocketAddress(minecraftServer.status.address, minecraftServer.status.ports!![0].port!!),
                tags?.split(",")?.toSet().orEmpty()
            )
        }
    }

    private fun registerServer(name: String, address: InetSocketAddress, tags: Set<String>) {
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
}

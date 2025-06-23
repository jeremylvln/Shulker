package io.shulkermc.agent.services

import io.shulkermc.agent.adapters.cache.CacheAdapter
import io.shulkermc.agent.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.agent.adapters.kubernetes.models.AgonesV1GameServer
import io.shulkermc.agent.adapters.kubernetes.models.WatchAction
import java.util.logging.Logger

abstract class ServerDirectoryService(
    private val kubernetesGateway: KubernetesGatewayAdapter,
    private val cache: CacheAdapter,
    val logger: Logger
) {
    private val serversByTag = HashMap<String, MutableSet<String>>()
    private val tagsByServer = HashMap<String, Set<String>>()


    init {
        this.kubernetesGateway.watchMinecraftServerEvents { action, minecraftServer ->
            this.logger.fine("Detected modification on MinecraftServer '${minecraftServer.metadata.name}'")
            if (action == WatchAction.ADDED || action == WatchAction.MODIFIED) {
                this.registerServer(minecraftServer)
            } else if (action == WatchAction.DELETED) {
                this.unregisterServer(minecraftServer.metadata.name)
            }
        }

        val existingMinecraftServers = this.kubernetesGateway.listMinecraftServers()
        existingMinecraftServers.items
            .filterNotNull()
            .forEach(this::registerServer)
    }

    fun getServersByTag(tag: String): Set<String> = this.serversByTag.getOrDefault(tag, setOf())

    open fun registerServer(minecraftServer: AgonesV1GameServer) {
        if (minecraftServer.status == null) {
            return
        }

        if (minecraftServer.status.isReady()) {
            val tags = minecraftServer.metadata.annotations["minecraftserver.shulkermc.io/tags"]

            this.registerServer(
                minecraftServer.metadata.name,
                tags?.split(",")?.toSet().orEmpty(),
            )
        }
    }

    fun registerServer(
        name: String,
        tags: Set<String>,
    ) {
        for (tag in tags) {
            this.serversByTag.getOrPut(tag) { mutableSetOf() }.add(name)
            this.logger.fine("Tagged '$tag' on server '$name'")
        }
        this.tagsByServer[name] = tags
        this.logger.info("Added server '$name' to directory")
    }

    open fun unregisterServer(name: String) {
        val tags = this.tagsByServer[name]
        if (tags != null) {
            tags.forEach { tag -> this.serversByTag[tag]!!.remove(name) }
            this.tagsByServer.remove(name)
        }
    }

}

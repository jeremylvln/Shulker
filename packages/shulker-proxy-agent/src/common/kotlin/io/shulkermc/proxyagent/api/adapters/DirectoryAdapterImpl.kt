package io.shulkermc.proxyagent.api.adapters

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyapi.adapters.DirectoryAdapter
import io.shulkermc.proxyapi.adapters.ServerName
import io.shulkermc.proxyapi.adapters.ServerTag
import java.net.InetSocketAddress

class DirectoryAdapterImpl(
    private val agent: ShulkerProxyAgentCommon
) : DirectoryAdapter {
    private val serversByTag = HashMap<ServerTag, MutableSet<ServerName>>()
    private val tagsByServer = HashMap<ServerName, Set<ServerTag>>()

    fun registerServer(name: ServerName, address: InetSocketAddress, tags: Set<ServerTag>) {
        this.agent.proxyInterface.registerServer(name, address)

        for (tag in tags) {
            this.serversByTag.getOrPut(tag) { mutableSetOf() }.add(name)
            this.agent.logger.fine("Tagged '$tag' on server '$name'")
        }
        this.tagsByServer[name] = tags
        this.agent.logger.info("Added server '$name' to directory")
    }

    fun unregisterServer(name: ServerName) {
        if (this.agent.proxyInterface.unregisterServer(name)) {
            this.agent.logger.info("Removed server '$name' from directory")

            val tags = this.tagsByServer[name]
            if (tags != null) {
                tags.forEach { tag -> this.serversByTag[tag]!!.remove(name) }
                this.tagsByServer.remove(name)
            }
        }
    }

    override fun getServersByTag(tag: String): Set<ServerName> {
        return this.serversByTag.getOrDefault(tag, HashSet())
    }
}

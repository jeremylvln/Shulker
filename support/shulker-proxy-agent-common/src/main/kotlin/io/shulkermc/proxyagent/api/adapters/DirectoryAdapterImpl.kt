package io.shulkermc.proxyagent.api.adapters

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyapi.adapters.DirectoryAdapter
import io.shulkermc.proxyapi.adapters.ServerName
import io.shulkermc.proxyapi.adapters.ServerTag
import java.net.InetSocketAddress

class DirectoryAdapterImpl(
    private val agent: ShulkerProxyAgentCommon
): DirectoryAdapter {
    private val serversByTag = HashMap<ServerTag, MutableSet<ServerName>>()
    private val tagsByServer = HashMap<ServerName, Set<ServerTag>>()

    fun registerServer(name: ServerName, address: InetSocketAddress, tags: Set<ServerTag>) {
        this.agent.logger.info("Registering server '${name}' to directory")

        this.agent.proxyInterface.registerServer(name, address)

        for (tag in tags) {
            this.serversByTag.getOrPut(tag) { HashSet() }.add(name)
            this.agent.logger.fine("Tagged '$tag' on server '${name}'")
        }
        this.tagsByServer[name] = tags
    }

    fun unregisterServer(name: ServerName) {
        this.agent.logger.info("Unregistering server '$name' from directory")

        if (this.serversByTag.containsKey(name)) {
            val tags = this.tagsByServer[name]
            if (tags != null) {
                for (tag in tags) {
                    this.serversByTag[tag]!!.remove(name)
                    this.agent.logger.fine("Untagged '$tag' from server '${name}'")
                }
            }
            this.tagsByServer.remove(name)
        }

        this.agent.proxyInterface.unregisterServer(name)
    }

    override fun getServersByTag(tag: String): Set<ServerName> {
        return this.serversByTag.getOrDefault(tag, HashSet())
    }
}

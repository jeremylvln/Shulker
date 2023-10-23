package io.shulkermc.proxyagent.api

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import java.net.InetSocketAddress

class ShulkerProxyAPIImpl(private val agent: ShulkerProxyAgentCommon) : ShulkerProxyAPI() {
    private val serversByTag = HashMap<String, MutableSet<String>>()
    private val tagsByServer = HashMap<String, Set<String>>()

    init {
        INSTANCE = this
    }

    override fun getServersByTag(tag: String): Set<String> {
        return this.serversByTag.getOrDefault(tag, HashSet())
    }

    fun registerServer(name: String, address: InetSocketAddress, tags: Set<String>) {
        this.agent.proxyInterface.registerServer(name, address)

        for (tag in tags) {
            this.serversByTag.getOrPut(tag) { mutableSetOf() }.add(name)
            this.agent.logger.fine("Tagged '$tag' on server '$name'")
        }
        this.tagsByServer[name] = tags
        this.agent.logger.info("Added server '$name' to directory")
    }

    fun unregisterServer(name: String) {
        if (this.agent.proxyInterface.unregisterServer(name)) {
            this.agent.logger.info("Removed server '$name' from directory")

            val tags = this.tagsByServer[name]
            if (tags != null) {
                tags.forEach { tag -> this.serversByTag[tag]!!.remove(name) }
                this.tagsByServer.remove(name)
            }
        }
    }
}

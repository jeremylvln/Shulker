package io.shulkermc.proxy.api

import io.shulkermc.proxy.ShulkerProxyAgentCommon

class ShulkerProxyAPIImpl(private val agent: ShulkerProxyAgentCommon) : ShulkerProxyAPI() {
    override fun askShutdown() = this.agent.shutdown()

    override fun getServersByTag(tag: String): Set<String> = this.agent.serverDirectoryService.getServersByTag(tag)
}

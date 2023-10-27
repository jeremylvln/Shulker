package io.shulkermc.proxyagent.api

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon

class ShulkerProxyAPIImpl(private val agent: ShulkerProxyAgentCommon) : ShulkerProxyAPI() {
    override fun getServersByTag(tag: String): Set<String> = this.agent.serverDirectoryService.getServersByTag(tag)
}

package io.shulkermc.proxyagent.bungeecord

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import net.md_5.bungee.api.plugin.Plugin

@Suppress("unused")
class ShulkerProxyAgentBungeeCord : Plugin() {
    private val agent = ShulkerProxyAgentCommon(ProxyInterfaceBungeeCord(this, this.proxy), this.logger)

    override fun onEnable() {
        this.agent.onProxyInitialization()
    }

    override fun onDisable() {
        this.agent.onProxyShutdown()
    }
}

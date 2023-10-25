package io.shulkermc.proxyagent

import net.md_5.bungee.api.plugin.Plugin

class ShulkerProxyAgent : Plugin() {
    private val agent = ShulkerProxyAgentCommon(ProxyInterfaceBungeeCord(this, this.proxy), this.logger)

    override fun onEnable() {
        this.agent.onProxyInitialization()
    }

    override fun onDisable() {
        this.agent.onProxyShutdown()
    }
}

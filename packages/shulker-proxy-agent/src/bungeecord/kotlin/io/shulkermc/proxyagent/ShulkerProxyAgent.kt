package io.shulkermc.proxyagent

import net.md_5.bungee.api.plugin.Plugin

class ShulkerProxyAgent : Plugin() {
    private val proxyInterface = ProxyInterfaceBungeeCord(this, this.proxy)
    private val common = ShulkerProxyAgentCommon(this.proxyInterface, this.logger)

    override fun onEnable() {
        this.common.onProxyInitialization()
    }

    override fun onDisable() {
        this.common.onProxyShutdown()
    }
}

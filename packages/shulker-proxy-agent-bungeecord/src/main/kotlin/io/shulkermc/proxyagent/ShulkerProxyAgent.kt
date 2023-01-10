package io.shulkermc.proxyagent

import io.shulkermc.proxyapi.ShulkerProxyAPI
import net.md_5.bungee.api.plugin.Plugin

class ShulkerProxyAgent : Plugin() {
    private lateinit var common: ShulkerProxyAgentCommon

    override fun onEnable() {
        val proxyInterface = ProxyInterfaceBungeeCord(this, this.proxy)
        this.common = ShulkerProxyAgentCommon(proxyInterface, this.logger)
        this.common.onProxyInitialization()
    }

    override fun onDisable() {
        this.common.onProxyShutdown()
    }

    fun getAPI(): ShulkerProxyAPI =
        this.common.api
}

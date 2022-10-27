package io.shulkermc.proxyagent

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.proxy.ProxyServer
import java.util.logging.Logger

class ShulkerProxyAgent(
    plugin: BootstrapPlugin,
    proxy: ProxyServer,
    logger: Logger
) {
    private val proxyInterface = ProxyInterfaceVelocity(plugin, proxy)
    private val common = ShulkerProxyAgentCommon(this.proxyInterface, logger)

    fun onProxyInitialization(@Suppress("UNUSED_PARAMETER") event: ProxyInitializeEvent) {
        this.common.onProxyInitialization()
    }

    fun onProxyShutdown(@Suppress("UNUSED_PARAMETER") event: ProxyShutdownEvent) {
        this.common.onProxyShutdown()
    }
}

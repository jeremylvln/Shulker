package io.shulkermc.proxyagent

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import java.util.logging.Logger

@Plugin(
    id = "shulker-proxy-agent",
    name = "ShulkerProxyAgent",
    version = VelocityBuildConfig.VERSION,
    authors = ["Jérémy Levilain <jeremy@jeremylvln.fr>"]
)
class ShulkerProxyAgent @Inject constructor(
    proxy: ProxyServer,
    logger: Logger
) {
    private val proxyInterface = ProxyInterfaceVelocity(this, proxy)
    private val common = ShulkerProxyAgentCommon(this.proxyInterface, logger)

    @Subscribe
    fun onProxyInitialization(@Suppress("UNUSED_PARAMETER") event: ProxyInitializeEvent) {
        this.common.onProxyInitialization()
    }

    @Subscribe
    fun onProxyShutdown(@Suppress("UNUSED_PARAMETER") event: ProxyShutdownEvent) {
        this.common.onProxyShutdown()
    }
}

package io.shulkermc.proxyagent.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.VelocityBuildConfig
import io.shulkermc.proxyagent.velocity.commands.GlobalControlCommand
import io.shulkermc.proxyagent.velocity.commands.GlobalFindCommand
import io.shulkermc.proxyagent.velocity.commands.GlobalListCommand
import io.shulkermc.proxyagent.velocity.commands.GlobalTeleportCommand
import java.util.logging.Logger

@Plugin(
    id = "shulker-proxy-agent",
    name = "ShulkerProxyAgent",
    version = VelocityBuildConfig.VERSION,
    authors = ["Jérémy Levilain <jeremy@jeremylvln.fr>"],
)
class ShulkerProxyAgentVelocity
    @Inject
    constructor(
        val proxy: ProxyServer,
        logger: Logger,
    ) {
        val agent = ShulkerProxyAgentCommon(ProxyInterfaceVelocity(this, proxy), logger)

        @Subscribe
        fun onProxyInitialization(
            @Suppress("UNUSED_PARAMETER") event: ProxyInitializeEvent,
        ) {
            this.agent.onProxyInitialization()

            GlobalListCommand.register(this)
            GlobalTeleportCommand.register(this)
            GlobalFindCommand.register(this)
            GlobalControlCommand.register(this)
        }

        @Subscribe
        fun onProxyShutdown(
            @Suppress("UNUSED_PARAMETER") event: ProxyShutdownEvent,
        ) {
            this.agent.onProxyShutdown()
        }
    }

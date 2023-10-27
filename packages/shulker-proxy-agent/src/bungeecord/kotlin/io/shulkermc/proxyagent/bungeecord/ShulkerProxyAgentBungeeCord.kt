package io.shulkermc.proxyagent.bungeecord

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.bungeecord.commands.GlobalFindCommand
import io.shulkermc.proxyagent.bungeecord.commands.GlobalListCommand
import io.shulkermc.proxyagent.bungeecord.commands.GlobalTeleportCommand
import net.md_5.bungee.api.plugin.Plugin

@Suppress("unused")
class ShulkerProxyAgentBungeeCord : Plugin() {
    private val agent = ShulkerProxyAgentCommon(ProxyInterfaceBungeeCord(this, this.proxy), this.logger)

    override fun onEnable() {
        this.agent.onProxyInitialization()

        this.proxy.pluginManager.registerCommand(this, GlobalListCommand(this.agent, this.proxy))
        this.proxy.pluginManager.registerCommand(this, GlobalTeleportCommand(this.agent))
        this.proxy.pluginManager.registerCommand(this, GlobalFindCommand(this.agent))
    }

    override fun onDisable() {
        this.agent.onProxyShutdown()
    }
}

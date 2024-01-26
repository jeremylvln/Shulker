package io.shulkermc.proxyagent.bungeecord

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.bungeecord.commands.GlobalFindCommand
import io.shulkermc.proxyagent.bungeecord.commands.GlobalListCommand
import io.shulkermc.proxyagent.bungeecord.commands.GlobalTeleportCommand
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.md_5.bungee.api.plugin.Plugin

@Suppress("unused")
class ShulkerProxyAgentBungeeCord : Plugin() {
    val agent = ShulkerProxyAgentCommon(ProxyInterfaceBungeeCord(this, this.proxy), this.logger)
    private lateinit var adventure: BungeeAudiences

    override fun onEnable() {
        this.adventure = BungeeAudiences.create(this)
        this.agent.onProxyInitialization()

        this.proxy.pluginManager.registerCommand(this, GlobalListCommand(this.agent, this.adventure, this.proxy))
        this.proxy.pluginManager.registerCommand(this, GlobalTeleportCommand(this.agent, this.adventure))
        this.proxy.pluginManager.registerCommand(this, GlobalFindCommand(this.agent, this.adventure))
    }

    override fun onDisable() {
        this.adventure.close()
        this.agent.onProxyShutdown()
    }
}

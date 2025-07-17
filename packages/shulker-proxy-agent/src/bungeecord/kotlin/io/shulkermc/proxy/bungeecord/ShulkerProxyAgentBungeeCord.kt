package io.shulkermc.proxy.bungeecord

import io.shulkermc.proxy.ShulkerProxyAgentCommon
import io.shulkermc.proxy.bungeecord.commands.GlobalControlCommand
import io.shulkermc.proxy.bungeecord.commands.GlobalFindCommand
import io.shulkermc.proxy.bungeecord.commands.GlobalListCommand
import io.shulkermc.proxy.bungeecord.commands.GlobalTeleportCommand
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
        this.proxy.pluginManager.registerCommand(this, GlobalControlCommand(this.agent, this.adventure))
    }

    override fun onDisable() {
        this.adventure.close()
        this.agent.onProxyShutdown()
    }
}

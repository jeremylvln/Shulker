package io.shulkermc.serveragent

import org.bukkit.plugin.java.JavaPlugin

class ShulkerServerAgent : JavaPlugin() {
    private val agent = ShulkerServerAgentCommon(ServerInterfacePaper(this), this.getLogger())

    override fun onEnable() {
        this.agent.onServerInitialization()
    }

    override fun onDisable() {
        this.agent.onServerShutdown()
    }
}

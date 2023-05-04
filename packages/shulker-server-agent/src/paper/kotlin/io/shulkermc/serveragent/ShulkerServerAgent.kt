package io.shulkermc.serveragent

import org.bukkit.plugin.java.JavaPlugin

class ShulkerServerAgent : JavaPlugin() {
    private val common = ShulkerServerAgentCommon(this.getLogger())

    override fun onEnable() {
        this.common.onServerInitialization()
    }

    override fun onDisable() {
        this.common.onServerShutdown()
    }
}

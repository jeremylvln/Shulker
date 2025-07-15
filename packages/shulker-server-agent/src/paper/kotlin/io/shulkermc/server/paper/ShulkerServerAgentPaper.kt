package io.shulkermc.server.paper

import io.shulkermc.server.ShulkerServerAgentCommon
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class ShulkerServerAgentPaper : JavaPlugin() {
    private val agent = ShulkerServerAgentCommon(ServerInterfacePaper(this), this.getLogger())

    override fun onEnable() {
        this.agent.onServerInitialization()
    }

    override fun onDisable() {
        this.agent.onServerShutdown()
    }
}

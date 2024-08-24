package io.shulkermc.proxyagent.commands

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object ControlCommandHandler {
    const val NAME = "shulker:ctl"
    const val PERMISSION = "shulker.command.ctl"

    fun executeDrainProxy(
        agent: ShulkerProxyAgentCommon,
        source: Audience,
        proxyName: String,
    ) {
        agent.pubSub.drainProxy(proxyName)
        source.sendMessage(Component.text("Sent drain request to proxy $proxyName", NamedTextColor.GREEN))
    }

    fun executeReconnectProxy(
        agent: ShulkerProxyAgentCommon,
        source: Audience,
        proxyName: String,
    ) {
        agent.pubSub.reconnectProxy(proxyName)
        source.sendMessage(Component.text("Sent reconnect request to proxy $proxyName", NamedTextColor.GREEN))
    }
}

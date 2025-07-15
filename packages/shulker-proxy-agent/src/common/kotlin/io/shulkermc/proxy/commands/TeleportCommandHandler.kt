package io.shulkermc.proxy.commands

import io.shulkermc.proxy.ShulkerProxyAgentCommon
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import kotlin.jvm.optionals.getOrNull

object TeleportCommandHandler {
    const val NAME = "gtp"
    const val PERMISSION = "shulker.command.gtp"

    fun executeTeleportToPlayer(
        agent: ShulkerProxyAgentCommon,
        source: Audience,
        playerName: String,
    ) {
        val (playerId, playerPosition) = CommandHandlerHelper.findPlayerOrMessage(agent, source, playerName).getOrNull() ?: return
        val server = playerPosition.serverName

        agent.cluster.pubSub.teleportPlayerOnServer(playerId, server)
        source.sendMessage(Component.text("Teleported to server $server", NamedTextColor.GREEN))
    }

    fun executeTeleportPlayerToServer(
        agent: ShulkerProxyAgentCommon,
        source: Audience,
        playerName: String,
        serverName: String,
    ) {
        val (playerId, _) = CommandHandlerHelper.findPlayerOrMessage(agent, source, playerName).getOrNull() ?: return

        agent.cluster.pubSub.teleportPlayerOnServer(playerId, serverName)
        source.sendMessage(
            Component.text("Teleported $playerName to server $serverName", NamedTextColor.GREEN),
        )
    }
}

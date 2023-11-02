package io.shulkermc.proxyagent.commands

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object ListCommandHandler {
    const val NAME = "glist"
    const val PERMISSION = "shulker.command.glist"

    fun executeListOnServers(agent: ShulkerProxyAgentCommon, source: Audience, serverNames: Set<String>) {
        source.sendMessage(
            Component.text("List of players online on ${serverNames.size} servers:")
                .color(NamedTextColor.YELLOW)
        )

        serverNames.mapIndexed { index, serverName ->
            val boxCharacter = if (index == serverNames.size - 1) "└" else "├"
            val playerNames = agent.cache.getPlayerNamesFromIds(agent.cache.listPlayersInServer(serverName)).values
            val playerNamesJoined = playerNames
                .sortedBy { it.lowercase() }
                .joinToString(", ") { it }

            source.sendMessage(
                Component.text("$boxCharacter ")
                    .color(NamedTextColor.DARK_GRAY)
                    .append(Component.text(serverName).color(NamedTextColor.YELLOW))
                    .append(Component.text(" (${playerNames.size})").color(NamedTextColor.YELLOW))
                    .append(Component.text(": ").color(NamedTextColor.WHITE))
                    .append(Component.text(playerNamesJoined).color(NamedTextColor.WHITE))
            )
        }
    }
}

package io.shulkermc.proxyagent.commands

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.api.ShulkerProxyAPI.PlayerPosition
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.Optional

object CommandHandlerHelper {
    fun findPlayerOrMessage(
        agent: ShulkerProxyAgentCommon,
        source: Audience,
        playerName: String,
    ): Optional<PlayerPosition> {
        val playerPosition =
            agent.cache.getPlayerIdFromName(playerName)
                .flatMap { playerId -> agent.cache.getPlayerPosition(playerId) }

        if (playerPosition.isEmpty) {
            source.sendMessage(Component.text("Player $playerName not found", NamedTextColor.RED))
        }

        return playerPosition
    }
}

package io.shulkermc.proxyagent.commands

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.api.ShulkerProxyAPI.PlayerPosition
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.Optional
import java.util.UUID

object CommandHandlerHelper {
    fun findPlayerOrMessage(
        agent: ShulkerProxyAgentCommon,
        source: Audience,
        playerName: String,
    ): Optional<Pair<UUID, PlayerPosition>> {
        val playerId = agent.cache.getPlayerIdFromName(playerName)
        val playerPosition = playerId.flatMap { agent.cache.getPlayerPosition(it) }

        if (playerId.isEmpty || playerPosition.isEmpty) {
            source.sendMessage(Component.text("Player $playerName not found", NamedTextColor.RED))
            return Optional.empty()
        }

        return Optional.of(Pair(playerId.get(), playerPosition.get()))
    }
}

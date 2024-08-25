package io.shulkermc.proxyagent.commands

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import kotlin.jvm.optionals.getOrNull

object FindCommandHandler {
    const val NAME = "gfind"
    const val PERMISSION = "shulker.command.gfind"

    fun executeFind(
        agent: ShulkerProxyAgentCommon,
        source: Audience,
        playerName: String,
    ) {
        val (_, playerPosition) = CommandHandlerHelper.findPlayerOrMessage(agent, source, playerName).getOrNull() ?: return

        source.sendMessage(
            Component.text(
                "Player $playerName is connected on proxy ${playerPosition.proxyName} and located on server ${playerPosition.serverName}",
                NamedTextColor.YELLOW,
            ),
        )
    }
}

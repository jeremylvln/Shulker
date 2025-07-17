package io.shulkermc.proxy.commands

import io.shulkermc.proxy.ShulkerProxyAgentCommon
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import kotlin.jvm.optionals.getOrNull

object KickCommandHandler {
    const val NAME = "gkick"
    const val PERMISSION = "shulker.command.gkick"

    private val DEFAULT_KICK_MESSAGE = Component.text("You were kicked by an operator")

    fun executeKick(
        agent: ShulkerProxyAgentCommon,
        source: Audience,
        playerName: String,
        message: Component?,
    ) {
        val (playerId, _) = CommandHandlerHelper.findPlayerOrMessage(agent, source, playerName).getOrNull() ?: return
        agent.cluster.pubSub.disconnectPlayerFromCluster(playerId, message ?: DEFAULT_KICK_MESSAGE)
        source.sendMessage(Component.text("Kicked player $playerName", NamedTextColor.YELLOW))
    }
}

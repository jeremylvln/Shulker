package io.shulkermc.proxyagent.velocity.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object GlobalFindCommand {
    fun create(agent: ShulkerProxyAgentCommon): BrigadierCommand {
        val rootNode = LiteralArgumentBuilder.literal<CommandSource>("gfind")
            .requires { it.hasPermission("shulker.command.gfind") }
            .then(
                RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.word())
                    .executes { context ->
                        val source = context.source
                        val player = context.getArgument("player", String::class.java)
                        val playerPosition = agent.cache.getPlayerIdFromName(player)
                            .flatMap { playerId -> agent.cache.getPlayerPosition(playerId) }

                        if (playerPosition.isEmpty) {
                            source.sendMessage(Component.text("Player $player not found.", NamedTextColor.RED))
                            return@executes Command.SINGLE_SUCCESS
                        }

                        source.sendMessage(
                            Component.text(
                                "Player $player is connected on proxy ${playerPosition.get().proxyName} and located on server ${playerPosition.get().serverName}.", // ktlint-disable standard_max-line-length
                                NamedTextColor.GREEN
                            )
                        )
                        return@executes Command.SINGLE_SUCCESS
                    }
            )
            .build()

        return BrigadierCommand(rootNode)
    }
}

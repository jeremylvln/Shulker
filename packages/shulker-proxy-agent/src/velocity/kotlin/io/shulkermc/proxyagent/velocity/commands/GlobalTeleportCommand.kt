package io.shulkermc.proxyagent.velocity.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object GlobalTeleportCommand {
    fun create(agent: ShulkerProxyAgentCommon, proxyServer: ProxyServer): BrigadierCommand {
        val rootNode = LiteralArgumentBuilder.literal<CommandSource>("gtp")
            .requires { it.hasPermission("shulker.command.gtp") }
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

                        val server = playerPosition.get().serverName
                        agent.pubSub.teleportPlayerOnServer(player, server)
                        source.sendMessage(Component.text("Teleported to server $server.", NamedTextColor.GREEN))

                        return@executes Command.SINGLE_SUCCESS
                    }
                    .then(
                        RequiredArgumentBuilder.argument<CommandSource, String>("server", StringArgumentType.word())
                            .suggests { _, builder ->
                                proxyServer.allServers.forEach { server ->
                                    builder.suggest(server.serverInfo.name)
                                }

                                return@suggests builder.buildFuture()
                            }
                            .executes { context ->
                                val source = context.source
                                val player = context.getArgument("player", String::class.java)
                                val server = context.getArgument("server", String::class.java)

                                agent.pubSub.teleportPlayerOnServer(player, server)
                                source.sendMessage(Component.text("Teleported $player to server $server.", NamedTextColor.GREEN))

                                return@executes Command.SINGLE_SUCCESS
                            }
                    )
            )
            .build()

        return BrigadierCommand(rootNode)
    }
}

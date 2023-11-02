package io.shulkermc.proxyagent.velocity.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.commands.TeleportCommandHandler
import io.shulkermc.proxyagent.velocity.ShulkerProxyAgentVelocity

object GlobalTeleportCommand {
    fun register(plugin: ShulkerProxyAgentVelocity) {
        plugin.proxy.commandManager.register(
            plugin.proxy.commandManager.metaBuilder(TeleportCommandHandler.NAME).plugin(plugin).build(),
            createCommand(plugin.agent, plugin.proxy)
        )
    }

    private fun createCommand(agent: ShulkerProxyAgentCommon, proxyServer: ProxyServer): BrigadierCommand {
        val rootNode = LiteralArgumentBuilder.literal<CommandSource>(TeleportCommandHandler.NAME)
            .requires { it.hasPermission(TeleportCommandHandler.PERMISSION) }
            .then(
                RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.word())
                    .executes { context ->
                        val source = context.source
                        val playerName = context.getArgument("player", String::class.java)

                        TeleportCommandHandler.executeTeleportToPlayer(agent, source, playerName)
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
                                val playerName = context.getArgument("player", String::class.java)
                                val serverName = context.getArgument("server", String::class.java)

                                TeleportCommandHandler.executeTeleportPlayerToServer(
                                    agent,
                                    source,
                                    playerName,
                                    serverName
                                )
                                return@executes Command.SINGLE_SUCCESS
                            }
                    )
            )
            .build()

        return BrigadierCommand(rootNode)
    }
}

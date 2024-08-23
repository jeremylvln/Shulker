package io.shulkermc.proxyagent.velocity.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.commands.ListCommandHandler
import io.shulkermc.proxyagent.velocity.ShulkerProxyAgentVelocity

object GlobalListCommand {
    fun register(plugin: ShulkerProxyAgentVelocity) {
        plugin.proxy.commandManager.register(
            plugin.proxy.commandManager.metaBuilder(ListCommandHandler.NAME).plugin(plugin).build(),
            createCommand(plugin.agent, plugin.proxy),
        )
    }

    private fun createCommand(
        agent: ShulkerProxyAgentCommon,
        proxyServer: ProxyServer,
    ): BrigadierCommand {
        val rootNode =
            LiteralArgumentBuilder.literal<CommandSource>(ListCommandHandler.NAME)
                .requires { it.hasPermission(ListCommandHandler.PERMISSION) }
                .executes { context ->
                    val source = context.source

                    ListCommandHandler.executeListOnServers(
                        agent,
                        source,
                        proxyServer.allServers.map { it.serverInfo.name }.toSet(),
                    )
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
                            val server = context.getArgument("server", String::class.java)

                            ListCommandHandler.executeListOnServers(agent, source, setOf(server))
                            return@executes Command.SINGLE_SUCCESS
                        },
                )
                .build()

        return BrigadierCommand(rootNode)
    }
}

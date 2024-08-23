package io.shulkermc.proxyagent.velocity.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.commands.ControlCommandHandler
import io.shulkermc.proxyagent.velocity.ShulkerProxyAgentVelocity

object GlobalControlCommand {
    fun register(plugin: ShulkerProxyAgentVelocity) {
        plugin.proxy.commandManager.register(
            plugin.proxy.commandManager.metaBuilder(ControlCommandHandler.NAME).plugin(plugin).build(),
            createCommand(plugin.agent),
        )
    }

    private fun createCommand(
        agent: ShulkerProxyAgentCommon,
    ): BrigadierCommand {
        val rootNode =
            LiteralArgumentBuilder.literal<CommandSource>(ControlCommandHandler.NAME)
                .requires { it.hasPermission(ControlCommandHandler.PERMISSION) }
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("drain")
                        .then(
                            RequiredArgumentBuilder.argument<CommandSource, String>("proxy", StringArgumentType.word())
                                .suggests { _, builder ->
                                    agent.cache.listRegisteredProxies().forEach { proxy ->
                                        builder.suggest(proxy.proxyName)
                                    }

                                    return@suggests builder.buildFuture()
                                }
                                .executes { context ->
                                    val source = context.source
                                    val proxyName = context.getArgument("proxy", String::class.java)

                                    ControlCommandHandler.executeDrainProxy(agent, source, proxyName)
                                    return@executes Command.SINGLE_SUCCESS
                                },
                        ),
                )
                .then(
                    LiteralArgumentBuilder.literal<CommandSource>("reconnect")
                        .then(
                            RequiredArgumentBuilder.argument<CommandSource, String>("proxy", StringArgumentType.word())
                                .suggests { _, builder ->
                                    agent.cache.listRegisteredProxies().forEach { proxy ->
                                        builder.suggest(proxy.proxyName)
                                    }

                                    return@suggests builder.buildFuture()
                                }
                                .executes { context ->
                                    val source = context.source
                                    val proxyName = context.getArgument("proxy", String::class.java)

                                    ControlCommandHandler.executeReconnectProxy(agent, source, proxyName)
                                    return@executes Command.SINGLE_SUCCESS
                                },
                        ),
                )
                .build()

        return BrigadierCommand(rootNode)
    }
}

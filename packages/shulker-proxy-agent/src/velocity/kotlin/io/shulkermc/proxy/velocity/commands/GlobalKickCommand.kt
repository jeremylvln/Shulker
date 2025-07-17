package io.shulkermc.proxy.velocity.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import io.shulkermc.proxy.ShulkerProxyAgentCommon
import io.shulkermc.proxy.commands.KickCommandHandler
import io.shulkermc.proxy.velocity.ShulkerProxyAgentVelocity
import net.kyori.adventure.text.Component

object GlobalKickCommand {
    fun register(plugin: ShulkerProxyAgentVelocity) {
        plugin.proxy.commandManager.register(
            plugin.proxy.commandManager.metaBuilder(KickCommandHandler.NAME).plugin(plugin).build(),
            createCommand(plugin.agent),
        )
    }

    private fun createCommand(agent: ShulkerProxyAgentCommon): BrigadierCommand {
        val rootNode =
            LiteralArgumentBuilder.literal<CommandSource>(KickCommandHandler.NAME)
                .requires { it.hasPermission(KickCommandHandler.PERMISSION) }
                .then(
                    RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.word())
                        .then(
                            RequiredArgumentBuilder.argument<CommandSource, String>("message", StringArgumentType.greedyString())
                                .executes { context ->
                                    val source = context.source
                                    val playerName = context.getArgument("player", String::class.java)
                                    val message = context.getArgument("message", String::class.java)
                                    KickCommandHandler.executeKick(agent, source, playerName, Component.text(message))
                                    return@executes Command.SINGLE_SUCCESS
                                }
                        )
                        .executes { context ->
                            val source = context.source
                            val playerName = context.getArgument("player", String::class.java)
                            KickCommandHandler.executeKick(agent, source, playerName, null)
                            return@executes Command.SINGLE_SUCCESS
                        },
                )
                .build()

        return BrigadierCommand(rootNode)
    }
}

package io.shulkermc.proxyagent.velocity.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.commands.FindCommandHandler
import io.shulkermc.proxyagent.velocity.ShulkerProxyAgentVelocity

object GlobalFindCommand {
    fun register(plugin: ShulkerProxyAgentVelocity) {
        plugin.proxy.commandManager.register(
            plugin.proxy.commandManager.metaBuilder(FindCommandHandler.NAME).plugin(plugin).build(),
            createCommand(plugin.agent),
        )
    }

    private fun createCommand(agent: ShulkerProxyAgentCommon): BrigadierCommand {
        val rootNode =
            LiteralArgumentBuilder.literal<CommandSource>(FindCommandHandler.NAME)
                .requires { it.hasPermission(FindCommandHandler.PERMISSION) }
                .then(
                    RequiredArgumentBuilder.argument<CommandSource, String>("player", StringArgumentType.word())
                        .executes { context ->
                            val source = context.source
                            val playerName = context.getArgument("player", String::class.java)
                            FindCommandHandler.executeFind(agent, source, playerName)
                            return@executes Command.SINGLE_SUCCESS
                        },
                )
                .build()

        return BrigadierCommand(rootNode)
    }
}

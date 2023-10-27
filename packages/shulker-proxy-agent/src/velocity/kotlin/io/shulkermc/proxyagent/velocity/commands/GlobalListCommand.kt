package io.shulkermc.proxyagent.velocity.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.utils.SEPARATOR
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

object GlobalListCommand {
    fun create(agent: ShulkerProxyAgentCommon, proxyServer: ProxyServer): BrigadierCommand {
        val rootNode = LiteralArgumentBuilder.literal<CommandSource>("glist")
            //.requires { it.hasPermission("shulker.command.glist") }
            .executes { context ->
                val source = context.source

                showPlayerListInServers(agent, source, proxyServer.allServers.map { it.serverInfo.name }.toSet())
                return@executes Command.SINGLE_SUCCESS
            }
            .then(RequiredArgumentBuilder.argument<CommandSource, String>("server", StringArgumentType.word())
                .suggests { _, builder ->
                    proxyServer.allServers.forEach { server ->
                        builder.suggest(server.serverInfo.name)
                    }

                    return@suggests builder.buildFuture()
                }
                .executes { context ->
                    val source = context.source
                    val server = context.getArgument("server", String::class.java)

                    showPlayerListInServers(agent, source, setOf(server))
                    return@executes Command.SINGLE_SUCCESS
                }
            )
            .build()

        return BrigadierCommand(rootNode)
    }

    private fun showPlayerListInServers(agent: ShulkerProxyAgentCommon, source: CommandSource, serverNames: Set<String>) {
        source.sendMessage(SEPARATOR)
        source.sendMessage(Component.empty())
        source.sendMessage(Component.text("  ◆ ")
            .color(NamedTextColor.LIGHT_PURPLE)
            .decorate(TextDecoration.BOLD)
            .append(Component.text("Global list of connected players").color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.BOLD, false)))
        source.sendMessage(Component.empty())

        serverNames.map { serverName ->
            source.sendMessage(this.createServerListMessage(agent, serverName))
            source.sendMessage(Component.empty())
        }

        source.sendMessage(SEPARATOR)
    }

    private fun createServerListMessage(agent: ShulkerProxyAgentCommon, serverName: String): Component {
        val playerNames = agent.cache.getPlayerNamesFromIds(agent.cache.listPlayersInServer(serverName)).values
            .sortedBy { it.lowercase() }
            .joinToString(", ") { it }

        return Component.text("$serverName:\n")
            .color(NamedTextColor.WHITE)
            .append(Component.text(playerNames).color(NamedTextColor.GRAY))
    }
}

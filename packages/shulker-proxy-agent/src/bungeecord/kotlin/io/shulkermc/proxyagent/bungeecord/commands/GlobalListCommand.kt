@file:Suppress("detekt:SpreadOperator")

package io.shulkermc.proxyagent.bungeecord.commands

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Command

class GlobalListCommand(private val agent: ShulkerProxyAgentCommon, private val proxyServer: ProxyServer) : Command(
    "glist",
    "shulker.command.glist"
) {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(
                *ComponentBuilder("You don't have permission to execute this command.").color(ChatColor.RED).create()
            )
            return
        }

        if (args.size != 1) {
            this.showPlayerListInServers(sender, this.proxyServer.servers.keys)
            return
        }

        val server = args[0]
        this.showPlayerListInServers(sender, setOf(server))
    }

    private fun showPlayerListInServers(sender: CommandSender, serverNames: Set<String>) {
        sender.sendMessage(*ComponentBuilder("⎯".repeat(63)).color(ChatColor.DARK_GRAY).strikethrough(true).create())
        sender.sendMessage(TextComponent(""))
        serverNames.map { serverName ->
            sender.sendMessage(*this.createServerListMessage(serverName))
            sender.sendMessage(TextComponent(""))
        }
        sender.sendMessage(*ComponentBuilder("⎯".repeat(63)).color(ChatColor.DARK_GRAY).strikethrough(true).create())
    }

    private fun createServerListMessage(serverName: String): Array<BaseComponent> {
        val playerNames = this.agent.cache.getPlayerNamesFromIds(
            this.agent.cache.listPlayersInServer(serverName)
        ).values
            .sortedBy { it.lowercase() }
            .joinToString(", ") { it }

        return ComponentBuilder("$serverName:\n")
            .color(ChatColor.WHITE)
            .append(ComponentBuilder(playerNames).color(ChatColor.GRAY).currentComponent)
            .create()
    }
}

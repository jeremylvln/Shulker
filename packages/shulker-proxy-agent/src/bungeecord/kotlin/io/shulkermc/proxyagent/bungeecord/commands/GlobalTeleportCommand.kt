package io.shulkermc.proxyagent.bungeecord.commands

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command

class GlobalTeleportCommand(private val agent: ShulkerProxyAgentCommon) : Command("gtp", "shulker.command.gtp") {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(*ComponentBuilder("You don't have permission to execute this command.").color(ChatColor.RED).create())
            return
        }

        if (args.isEmpty() || args.size > 2) {
            sender.sendMessage(*ComponentBuilder("Usage: /gtp <player> [server]").color(ChatColor.RED).create())
            return
        }

        val player = args[0]
        val playerPosition = this.agent.cache.getPlayerIdFromName(player)
            .flatMap { playerId -> this.agent.cache.getPlayerPosition(playerId) }

        if (playerPosition.isEmpty) {
            sender.sendMessage(*ComponentBuilder("Player $player not found.").color(ChatColor.RED).create())
            return
        }

        if (args.size == 1) {
            val server = playerPosition.get().serverName
            this.agent.pubSub.teleportPlayerOnServer(player, server)
            sender.sendMessage(*ComponentBuilder("Teleported to server $server.").color(ChatColor.GREEN).create())
        } else {
            val server = args[1]
            this.agent.pubSub.teleportPlayerOnServer(player, server)
            sender.sendMessage(*ComponentBuilder("Teleported $player to server $server.").color(ChatColor.GREEN).create())
        }
    }
}

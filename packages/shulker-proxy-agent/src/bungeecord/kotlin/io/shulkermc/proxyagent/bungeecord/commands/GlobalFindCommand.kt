package io.shulkermc.proxyagent.bungeecord.commands

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command

class GlobalFindCommand(private val agent: ShulkerProxyAgentCommon) : Command("gfind", "shulker.command.gfind") {
    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(
                *ComponentBuilder("You don't have permission to execute this command.").color(ChatColor.RED).create()
            )
            return
        }

        if (args.size != 1) {
            sender.sendMessage(*ComponentBuilder("Usage: /gfind <player>").color(ChatColor.RED).create())
            return
        }

        val player = args[0]
        val playerPosition = this.agent.cache.getPlayerIdFromName(player)
            .flatMap { playerId -> this.agent.cache.getPlayerPosition(playerId) }

        if (playerPosition.isEmpty) {
            sender.sendMessage(*ComponentBuilder("Player $player not found.").color(ChatColor.RED).create())
            return
        }

        sender.sendMessage(
            *ComponentBuilder(
                "Player $player is connected on proxy ${playerPosition.get().proxyName} and located on server ${playerPosition.get().serverName}." // ktlint-disable standard_max-line-length
            ).color(ChatColor.GREEN).create()
        )
    }
}

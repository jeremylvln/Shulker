package io.shulkermc.proxyagent.bungeecord.commands

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.commands.TeleportCommandHandler
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command

class GlobalTeleportCommand(
    private val agent: ShulkerProxyAgentCommon,
    private val adventure: BungeeAudiences
) : Command(TeleportCommandHandler.NAME, TeleportCommandHandler.PERMISSION) {
    companion object {
        private val USAGE_MESSAGE = Component.text("Usage: /gtp <player> [server]").color(NamedTextColor.RED)
    }

    override fun execute(sender: CommandSender, args: Array<out String>) {
        val audience = this.adventure.sender(sender)
        if (!BungeeCordCommandHelper.testPermissionOrMessage(sender, audience, this.permission)) {
            return
        }

        if (args.isEmpty() || args.size > 2) {
            audience.sendMessage(USAGE_MESSAGE)
            return
        }

        val playerName = args[0]

        if (args.size == 1) {
            TeleportCommandHandler.executeTeleportToPlayer(this.agent, audience, playerName)
        } else {
            val serverName = args[1]
            TeleportCommandHandler.executeTeleportPlayerToServer(this.agent, audience, playerName, serverName)
        }
    }
}

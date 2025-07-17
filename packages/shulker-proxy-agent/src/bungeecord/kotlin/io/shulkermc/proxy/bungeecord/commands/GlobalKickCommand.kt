package io.shulkermc.proxy.bungeecord.commands

import io.shulkermc.proxy.ShulkerProxyAgentCommon
import io.shulkermc.proxy.commands.KickCommandHandler
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command

class GlobalKickCommand(
    private val agent: ShulkerProxyAgentCommon,
    private val adventure: BungeeAudiences,
) : Command(KickCommandHandler.NAME, KickCommandHandler.PERMISSION) {
    companion object {
        private val USAGE_MESSAGE = Component.text("Usage: /gkick <player> [message]").color(NamedTextColor.RED)
    }

    override fun execute(
        sender: CommandSender,
        args: Array<out String>,
    ) {
        val audience = this.adventure.sender(sender)
        if (!BungeeCordCommandHelper.testPermissionOrMessage(sender, audience, this.permission)) {
            return
        }

        if (args.size != 1 && args.size != 2) {
            audience.sendMessage(USAGE_MESSAGE)
            return
        }

        val player = args[0]
        val message = if (args.size == 2) Component.text(args[1]) else null
        KickCommandHandler.executeKick(this.agent, audience, player, message)
    }
}

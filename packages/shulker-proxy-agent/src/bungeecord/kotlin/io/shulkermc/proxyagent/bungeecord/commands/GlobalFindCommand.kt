package io.shulkermc.proxyagent.bungeecord.commands

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.commands.FindCommandHandler
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command

class GlobalFindCommand(
    private val agent: ShulkerProxyAgentCommon,
    private val adventure: BungeeAudiences,
) : Command(FindCommandHandler.NAME, FindCommandHandler.PERMISSION) {
    companion object {
        private val USAGE_MESSAGE = Component.text("Usage: /gfind <player>").color(NamedTextColor.RED)
    }

    override fun execute(
        sender: CommandSender,
        args: Array<out String>,
    ) {
        val audience = this.adventure.sender(sender)
        if (!BungeeCordCommandHelper.testPermissionOrMessage(sender, audience, this.permission)) {
            return
        }

        if (args.size != 1) {
            audience.sendMessage(USAGE_MESSAGE)
            return
        }

        val player = args[0]
        FindCommandHandler.executeFind(this.agent, audience, player)
    }
}

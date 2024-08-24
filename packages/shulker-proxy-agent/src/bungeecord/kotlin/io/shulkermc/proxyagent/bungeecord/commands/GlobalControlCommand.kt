package io.shulkermc.proxyagent.bungeecord.commands

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.commands.ControlCommandHandler
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command

class GlobalControlCommand(
    private val agent: ShulkerProxyAgentCommon,
    private val adventure: BungeeAudiences,
) : Command(ControlCommandHandler.NAME, ControlCommandHandler.PERMISSION) {
    companion object {
        private val USAGE_MESSAGE =
            Component.text("Usage: /shulker:ctl <...>").color(NamedTextColor.RED)
                .appendNewline()
                .append(Component.text("- drain <proxy>"))
                .appendNewline()
                .append(Component.text("- reconnect <proxy>"))
    }

    override fun execute(
        sender: CommandSender,
        args: Array<out String>,
    ) {
        val audience = this.adventure.sender(sender)
        if (!BungeeCordCommandHelper.testPermissionOrMessage(sender, audience, this.permission)) {
            return
        }

        if (args.isEmpty()) {
            audience.sendMessage(USAGE_MESSAGE)
            return
        }

        val subArguments = args.copyOfRange(1, args.size)

        when (args[0]) {
            "drain" -> executeDrainSubcommand(audience, subArguments)
            "reconnect" -> this.executeReconnectSubcommand(audience, subArguments)
        }
    }

    private fun executeDrainSubcommand(
        audience: Audience,
        args: Array<out String>,
    ) {
        if (args.isEmpty()) {
            audience.sendMessage(USAGE_MESSAGE)
            return
        }

        val proxyName = args[0]
        ControlCommandHandler.executeDrainProxy(this.agent, audience, proxyName)
    }

    private fun executeReconnectSubcommand(
        audience: Audience,
        args: Array<out String>,
    ) {
        if (args.isEmpty()) {
            audience.sendMessage(USAGE_MESSAGE)
            return
        }

        val proxyName = args[0]
        ControlCommandHandler.executeReconnectProxy(this.agent, audience, proxyName)
    }
}

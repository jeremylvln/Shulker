package io.shulkermc.proxy.bungeecord.commands

import io.shulkermc.proxy.ShulkerProxyAgentCommon
import io.shulkermc.proxy.commands.ListCommandHandler
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Command

class GlobalListCommand(
    private val agent: ShulkerProxyAgentCommon,
    private val adventure: BungeeAudiences,
    private val proxyServer: ProxyServer,
) : Command(ListCommandHandler.NAME, ListCommandHandler.PERMISSION) {
    override fun execute(
        sender: CommandSender,
        args: Array<out String>,
    ) {
        val audience = this.adventure.sender(sender)
        if (!BungeeCordCommandHelper.testPermissionOrMessage(sender, audience, this.permission)) {
            return
        }

        if (args.size != 1) {
            ListCommandHandler.executeListOnServers(this.agent, audience, this.proxyServer.servers.keys)
            return
        }

        val serverName = args[0]
        ListCommandHandler.executeListOnServers(this.agent, audience, setOf(serverName))
    }
}

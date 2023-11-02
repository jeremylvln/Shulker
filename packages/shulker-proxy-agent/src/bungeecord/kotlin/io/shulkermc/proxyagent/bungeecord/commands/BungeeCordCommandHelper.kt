package io.shulkermc.proxyagent.bungeecord.commands

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.CommandSender

object BungeeCordCommandHelper {
    private val PERMISSION_MESSAGE = Component.text("You don't have permission to execute this command.")
        .color(NamedTextColor.RED)

    fun testPermissionOrMessage(sender: CommandSender, audience: Audience, permission: String): Boolean {
        if (!sender.hasPermission(permission)) {
            audience.sendMessage(PERMISSION_MESSAGE)
            return false
        }

        return true
    }
}

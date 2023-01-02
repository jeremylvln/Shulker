package io.shulkermc.proxyagent.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

fun createDisconnectMessage(message: String, color: TextColor): Component =
        Component.text("◆ Shulker ◆\n")
                .color(NamedTextColor.LIGHT_PURPLE)
                .decorate(TextDecoration.BOLD)
                .append(
                        Component.text(message)
                                .color(color)
                                .decorate(TextDecoration.BOLD)
                )

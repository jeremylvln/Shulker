package io.shulkermc.proxyagent.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

val SEPARATOR = Component.text("⎯".repeat(63)).color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.STRIKETHROUGH)

fun createDisconnectMessage(message: String, color: TextColor): Component =
    Component.text("◆ Shulker ◆\n")
        .color(NamedTextColor.LIGHT_PURPLE)
        .decorate(TextDecoration.BOLD)
        .append(
            Component.text(message)
                .color(color)
                .decorate(TextDecoration.BOLD)
        )

fun createBlockTitleMessage(title: String): Component =
    Component.text("  ◆ ")
        .color(NamedTextColor.LIGHT_PURPLE)
        .decorate(TextDecoration.BOLD)
        .append(Component.text(title).color(NamedTextColor.LIGHT_PURPLE))

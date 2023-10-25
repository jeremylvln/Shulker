package io.shulkermc.proxyagent.platform

import net.kyori.adventure.text.Component

interface Player {
    fun disconnect(component: Component)
}

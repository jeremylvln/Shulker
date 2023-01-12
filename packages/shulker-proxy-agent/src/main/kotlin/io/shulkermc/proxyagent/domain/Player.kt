package io.shulkermc.proxyagent.domain

import net.kyori.adventure.text.Component

interface Player {
    fun disconnect(component: Component)
}

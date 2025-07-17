package io.shulkermc.proxy.platform

import net.kyori.adventure.text.Component
import java.util.UUID

interface Player {
    val uniqueId: UUID
    val name: String

    fun disconnect(component: Component)
}

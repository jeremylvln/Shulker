package io.shulkermc.proxyagent.platform

import net.kyori.adventure.text.Component
import java.util.UUID

interface Player {
    val uniqueId: UUID
    fun disconnect(component: Component)
}

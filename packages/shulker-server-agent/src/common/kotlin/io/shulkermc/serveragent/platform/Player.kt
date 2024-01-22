package io.shulkermc.serveragent.platform

import java.util.UUID

interface Player {
    val uniqueId: UUID
    val name: String
}

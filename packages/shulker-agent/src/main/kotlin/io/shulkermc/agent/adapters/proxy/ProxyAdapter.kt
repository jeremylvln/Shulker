package io.shulkermc.agent.adapters.proxy

import java.util.UUID

interface ProxyAdapter {

    fun teleportPlayerOnServer(
        playerId: UUID,
        serverName: String,
    )
}

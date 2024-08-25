package io.shulkermc.proxyagent.adapters.pubsub

import java.util.UUID

interface PubSubAdapter {
    fun teleportPlayerOnServer(
        playerId: UUID,
        serverName: String,
    )

    fun onTeleportPlayerOnServer(callback: (playerId: UUID, serverName: String) -> Unit)

    fun drainProxy(proxyName: String)

    fun onDrainProxy(callback: (proxyName: String) -> Unit)

    fun reconnectProxy(proxyName: String)

    fun onReconnectProxy(callback: (proxyName: String) -> Unit)
}

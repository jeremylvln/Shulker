package io.shulkermc.proxyagent.adapters.pubsub

interface PubSubAdapter {
    fun teleportPlayerOnServer(
        playerId: String,
        serverName: String,
    )

    fun onTeleportPlayerOnServer(callback: (playerId: String, serverName: String) -> Unit)

    fun drainProxy(proxyName: String)

    fun onDrainProxy(callback: (proxyName: String) -> Unit)

    fun reconnectProxy(proxyName: String)

    fun onReconnectProxy(callback: (proxyName: String) -> Unit)
}

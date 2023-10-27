package io.shulkermc.proxyagent.adapters.pubsub

interface PubSubAdapter {
    fun teleportPlayerOnServer(playerId: String, serverName: String)
    fun onTeleportPlayerOnServer(callback: (playerId: String, serverName: String) -> Unit)
}

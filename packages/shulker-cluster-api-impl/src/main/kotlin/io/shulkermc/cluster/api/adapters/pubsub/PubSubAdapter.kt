package io.shulkermc.cluster.api.adapters.pubsub

import io.shulkermc.cluster.api.messaging.MessagingBus
import net.kyori.adventure.text.Component
import java.util.UUID

interface PubSubAdapter : MessagingBus {
    fun teleportPlayerOnServer(playerId: UUID, serverName: String)
    fun onTeleportPlayerOnServer(callback: (playerId: UUID, serverName: String) -> Unit)

    fun disconnectPlayerFromCluster(playerId: UUID, message: Component)
    fun onDisconnectPlayerFromCluster(callback: (playerId: UUID, message: Component) -> Unit)

    fun reconnectPlayerToCluster(playerId: UUID)
    fun onReconnectPlayerToCluster(callback: (playerId: UUID) -> Unit)

    fun drainProxy(proxyName: String)
    fun onDrainProxy(callback: (proxyName: String) -> Unit)
}

package io.shulkermc.proxy.handlers

import io.shulkermc.proxy.ShulkerProxyAgentCommon
import java.util.UUID

class ReconnectPlayerOnProxyHandler(private val agent: ShulkerProxyAgentCommon) {
    fun handle(playerId: UUID) {
        this.agent.playerMovementService.reconnectPlayerToCluster(playerId)
    }
}

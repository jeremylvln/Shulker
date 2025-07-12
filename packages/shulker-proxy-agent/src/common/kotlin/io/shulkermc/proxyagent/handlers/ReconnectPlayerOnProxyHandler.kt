package io.shulkermc.proxyagent.handlers

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import java.util.UUID

class ReconnectPlayerOnProxyHandler(private val agent: ShulkerProxyAgentCommon) {
    fun handle(playerId: UUID) {
        this.agent.playerMovementService.reconnectPlayerToCluster(playerId)
    }
}

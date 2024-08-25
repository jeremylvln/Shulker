package io.shulkermc.proxyagent.handlers

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import java.util.UUID

class TeleportPlayerOnServerHandler(private val agent: ShulkerProxyAgentCommon) {
    fun handle(
        playerId: UUID,
        serverName: String,
    ) {
        this.agent.proxyInterface.teleportPlayerOnServer(playerId, serverName)
    }
}

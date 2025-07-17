package io.shulkermc.proxy.handlers

import io.shulkermc.proxy.ShulkerProxyAgentCommon
import java.util.UUID

class TeleportPlayerOnServerHandler(private val agent: ShulkerProxyAgentCommon) {
    fun handle(
        playerId: UUID,
        serverName: String,
    ) {
        this.agent.proxyInterface.teleportPlayerOnServer(playerId, serverName)
    }
}

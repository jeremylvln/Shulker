package io.shulkermc.proxy.handlers

import io.shulkermc.proxy.ShulkerProxyAgentCommon
import net.kyori.adventure.text.Component
import java.util.UUID

class DisconnectPlayerFromClusterHandler(private val agent: ShulkerProxyAgentCommon) {
    fun handle(playerId: UUID, message: Component) {
        this.agent.proxyInterface.disconnectPlayer(playerId, message)
        this.agent.logger.info("Kicked player $playerId")
    }
}

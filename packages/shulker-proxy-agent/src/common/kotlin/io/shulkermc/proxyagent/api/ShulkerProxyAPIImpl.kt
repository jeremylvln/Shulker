package io.shulkermc.proxyagent.api

import io.shulkermc.agent.api.ShulkerAPI.PlayerPosition
import io.shulkermc.agent.api.ShulkerAPIHandler
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import java.util.Optional
import java.util.UUID

class ShulkerProxyAPIImpl(
    private val agent: ShulkerProxyAgentCommon,
    private val apiHandler: ShulkerAPIHandler
) : ShulkerProxyAPI() {
    override fun shutdown() = this.agent.shutdown()

    override fun reconnectPlayerToCluster(playerId: UUID) = this.agent.playerMovementService.reconnectPlayerToCluster(playerId)

    override fun getServersByTag(tag: String): Set<String> = this.agent.serverDirectoryService.getServersByTag(tag)

    override fun teleportPlayerOnServer(
        playerId: UUID,
        serverName: String,
    ) =
        this.agent.proxyInterface.teleportPlayerOnServer(playerId, serverName)

    override fun getPlayerPosition(playerId: UUID): Optional<PlayerPosition> =
        this.agent.cache.getPlayerPosition(
            playerId,
        )

    override fun isPlayerConnected(playerId: UUID): Boolean = this.agent.cache.isPlayerConnected(playerId)

    override fun countOnlinePlayers(): Int = this.agent.cache.countOnlinePlayers()

    override fun getPlayerIdFromName(playerName: String): Optional<UUID> = apiHandler.getPlayerIdFromName(playerName)

    override fun getPlayerNameFromId(playerId: UUID): Optional<String> = apiHandler.getPlayerNameFromId(playerId)
}

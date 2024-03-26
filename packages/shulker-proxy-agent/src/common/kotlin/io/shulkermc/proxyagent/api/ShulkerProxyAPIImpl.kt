package io.shulkermc.proxyagent.api

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import java.util.Optional
import java.util.UUID

class ShulkerProxyAPIImpl(private val agent: ShulkerProxyAgentCommon) : ShulkerProxyAPI() {
    override fun reconnectPlayerToCluster(playerId: UUID) = this.agent.proxyInterface.reconnectPlayerToCluster(playerId)

    override fun getServersByTag(tag: String): Set<String> = this.agent.serverDirectoryService.getServersByTag(tag)

    override fun getPlayerPosition(playerId: UUID): Optional<PlayerPosition> = this.agent.cache.getPlayerPosition(
        playerId
    )
    override fun isPlayerConnected(playerId: UUID): Boolean = this.agent.cache.isPlayerConnected(playerId)
    override fun countOnlinePlayers(): Int = this.agent.cache.countOnlinePlayers()

    override fun getPlayerIdFromName(playerName: String): Optional<UUID> {
        val cachedValue = this.agent.cache.getPlayerIdFromName(playerName)
        if (cachedValue.isPresent) return cachedValue

        val mojangProfile = this.agent.mojangGateway.getProfileFromName(playerName)
        if (mojangProfile.isPresent) {
            val playerId = mojangProfile.get().playerId
            this.agent.cache.updateCachedPlayerName(playerId, playerName)
            return Optional.of(playerId)
        }

        return Optional.empty()
    }

    override fun getPlayerNameFromId(playerId: UUID): Optional<String> {
        val cachedValue = this.agent.cache.getPlayerNameFromId(playerId)
        if (cachedValue.isPresent) return cachedValue

        val mojangProfile = this.agent.mojangGateway.getProfileFromId(playerId)
        if (mojangProfile.isPresent) {
            val playerName = mojangProfile.get().playerName
            this.agent.cache.updateCachedPlayerName(playerId, playerName)
            return Optional.of(playerName)
        }

        return Optional.empty()
    }
}

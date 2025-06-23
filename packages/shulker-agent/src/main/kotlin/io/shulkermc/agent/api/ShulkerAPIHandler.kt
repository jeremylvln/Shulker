package io.shulkermc.agent.api

import io.shulkermc.agent.adapters.cache.CacheAdapter
import io.shulkermc.agent.adapters.mojang.MojangGatewayAdapter
import java.util.Optional
import java.util.UUID

class ShulkerAPIHandler(
    private val cache: CacheAdapter,
    private val mojangGateway: MojangGatewayAdapter,
) {

    fun getPlayerIdFromName(playerName: String): Optional<UUID> {
        val cachedValue = this.cache.getPlayerIdFromName(playerName)
        if (cachedValue.isPresent) return cachedValue

        val mojangProfile = this.mojangGateway.getProfileFromName(playerName)
        if (mojangProfile.isPresent) {
            val playerId = mojangProfile.get().playerId
            this.cache.updateCachedPlayerName(playerId, playerName)
            return Optional.of(playerId)
        }

        return Optional.empty()
    }

    fun getPlayerNameFromId(playerId: UUID): Optional<String> {
        val cachedValue = this.cache.getPlayerNameFromId(playerId)
        if (cachedValue.isPresent) return cachedValue

        val mojangProfile = this.mojangGateway.getProfileFromId(playerId)
        if (mojangProfile.isPresent) {
            val playerName = mojangProfile.get().playerName
            this.cache.updateCachedPlayerName(playerId, playerName)
            return Optional.of(playerName)
        }

        return Optional.empty();
    }
}

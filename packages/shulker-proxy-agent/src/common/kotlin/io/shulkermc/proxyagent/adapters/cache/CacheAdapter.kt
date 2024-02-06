package io.shulkermc.proxyagent.adapters.cache

import io.shulkermc.proxyagent.api.ShulkerProxyAPI.PlayerPosition
import java.util.Optional
import java.util.UUID

interface CacheAdapter {
    fun registerProxy(proxyName: String, proxyCapacity: Int)
    fun unregisterProxy(proxyName: String)
    fun updateProxyLastSeen(proxyName: String)
    fun listRegisteredProxies(): List<RegisteredProxy>
    fun tryLockLostProxiesPurgeTask(ownerProxyName: String): Optional<Lock>

    fun unregisterServer(serverName: String)
    fun listPlayersInServer(serverName: String): List<UUID>

    fun setPlayerPosition(playerId: UUID, proxyName: String, serverName: String)
    fun unsetPlayerPosition(playerId: UUID)
    fun getPlayerPosition(playerId: UUID): Optional<PlayerPosition>
    fun isPlayerConnected(playerId: UUID): Boolean

    fun updateCachedPlayerName(playerId: UUID, playerName: String)
    fun getPlayerNameFromId(playerId: UUID): Optional<String>
    fun getPlayerIdFromName(playerName: String): Optional<UUID>
    fun getPlayerNamesFromIds(playerIds: List<UUID>): Map<UUID, String>

    fun countOnlinePlayers(): Int
    fun countPlayerCapacity(): Int

    data class RegisteredProxy(val proxyName: String, val proxyCapacity: Int, val lastSeenMillis: Long)

    interface Lock : AutoCloseable
}

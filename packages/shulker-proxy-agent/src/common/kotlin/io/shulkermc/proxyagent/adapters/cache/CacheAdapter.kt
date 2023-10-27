package io.shulkermc.proxyagent.adapters.cache

import io.shulkermc.proxyagent.api.ShulkerProxyAPI.PlayerPosition
import java.util.Optional
import java.util.UUID

interface CacheAdapter {
    fun registerProxy(name: String)
    fun unregisterProxy(name: String)
    fun updateProxyLastSeen(name: String)
    fun listRegisteredProxies(): List<RegisteredProxy>

    fun setPlayerPosition(playerId: UUID, proxyName: String, serverName: String)
    fun unsetPlayerPosition(playerId: UUID)
    fun getPlayerPosition(playerId: UUID): Optional<PlayerPosition>
    fun isPlayerConnected(playerId: UUID): Boolean

    fun tryLockLostProxiesPurgeTask(ownerProxyName: String, ttlSeconds: Long): Optional<Lock>

    data class RegisteredProxy(val proxyName: String, val lastSeenMillis: Long)

    interface Lock {
        fun release()
    }
}

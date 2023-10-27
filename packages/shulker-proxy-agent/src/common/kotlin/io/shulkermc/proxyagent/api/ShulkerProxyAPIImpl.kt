package io.shulkermc.proxyagent.api

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import java.util.Optional
import java.util.UUID

class ShulkerProxyAPIImpl(private val agent: ShulkerProxyAgentCommon) : ShulkerProxyAPI() {
    override fun getServersByTag(tag: String): Set<String> = this.agent.serverDirectoryService.getServersByTag(tag)

    override fun getPlayerPosition(playerId: UUID): Optional<PlayerPosition> = this.agent.cache.getPlayerPosition(playerId)
    override fun isPlayerConnected(playerId: UUID): Boolean = this.agent.cache.isPlayerConnected(playerId)
}

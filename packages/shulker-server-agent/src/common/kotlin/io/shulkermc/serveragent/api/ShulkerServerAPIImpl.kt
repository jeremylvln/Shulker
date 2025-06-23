package io.shulkermc.serveragent.api

import io.shulkermc.agent.api.PlayerPosition
import io.shulkermc.agent.api.ShulkerAPIHandler
import io.shulkermc.serveragent.ShulkerServerAgentCommon
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CompletableFuture

class ShulkerServerAPIImpl(
    private val agent: ShulkerServerAgentCommon,
    private val apiHandler: ShulkerAPIHandler
) : ShulkerServerAPI() {
    override fun askShutdown() = this.agent.shutdown()

    override fun setReady(): CompletableFuture<Void> = this.agent.agonesGateway.setReady().thenAccept {}

    override fun setAllocated(): CompletableFuture<Void> = this.agent.agonesGateway.setAllocated().thenAccept {}

    override fun setReserved(seconds: Long): CompletableFuture<Void> = this.agent.agonesGateway.setReserved(seconds).thenAccept {}

    override fun getServersByTag(tag: String): Set<String> = this.agent.serverDirectoryService.getServersByTag(tag)

    override fun countOnlinePlayers(): Int = this.agent.cache.countOnlinePlayers()

    override fun getPlayerPosition(playerId: UUID): Optional<PlayerPosition> = this.agent.cache.getPlayerPosition(playerId)

    override fun isPlayerConnected(playerId: UUID): Boolean = this.agent.cache.isPlayerConnected(playerId)

    override fun teleportPlayerOnServer(playerId: UUID, serverName: String) = this.agent.pubSub.teleportPlayerOnServer(playerId, serverName)

    override fun getPlayerIdFromName(playerName: String): Optional<UUID> = apiHandler.getPlayerIdFromName(playerName)

    override fun getPlayerNameFromId(playerId: UUID): Optional<String> = apiHandler.getPlayerNameFromId(playerId)
}

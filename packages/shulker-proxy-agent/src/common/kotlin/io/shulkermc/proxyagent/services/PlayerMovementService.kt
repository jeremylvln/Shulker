package io.shulkermc.proxyagent.services

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.platform.Player
import io.shulkermc.proxyagent.platform.PlayerPreLoginHookResult
import io.shulkermc.proxyagent.platform.ServerPreConnectHookResult
import io.shulkermc.proxyagent.utils.createDisconnectMessage
import net.kyori.adventure.text.format.NamedTextColor
import java.util.Optional

class PlayerMovementService(private val agent: ShulkerProxyAgentCommon) {
    companion object {
        private const val LIMBO_TAG = "limbo"

        private val MSG_NOT_ACCEPTING_PLAYERS = createDisconnectMessage(
            "Proxy is not accepting players, try reconnect.",
            NamedTextColor.RED
        )

        private val MSG_NO_LIMBO_FOUND = createDisconnectMessage(
            "No limbo server found, please check your cluster configuration.",
            NamedTextColor.RED
        )
    }

    private var acceptingPlayers = true

    init {
        this.agent.proxyInterface.addPlayerPreLoginHook { this.onPlayerPreLogin() }
        this.agent.proxyInterface.addServerPreConnectHook { player, originalServerName ->
            this.onServerPreConnect(player, originalServerName)
        }
    }

    fun setAcceptingPlayers(acceptingPlayers: Boolean) {
        this.acceptingPlayers = acceptingPlayers

        if (acceptingPlayers)
            this.agent.logger.info("Proxy is now accepting players")
        else
            this.agent.logger.info("Proxy is no longer accepting players")
    }

    private fun onPlayerPreLogin(): PlayerPreLoginHookResult {
        return if (!this.acceptingPlayers)
            PlayerPreLoginHookResult.disallow(MSG_NOT_ACCEPTING_PLAYERS)
        else
            PlayerPreLoginHookResult.allow()
    }

    private fun onServerPreConnect(player: Player, originalServerName: String): ServerPreConnectHookResult {
        if (originalServerName == LIMBO_TAG)
            return this.tryConnectToLimboOrDisconnect(player)
        return ServerPreConnectHookResult(Optional.empty())
    }

    private fun tryConnectToLimboOrDisconnect(player: Player): ServerPreConnectHookResult {
        val firstLimboServer = this.agent.serverDirectoryService.getServersByTag(LIMBO_TAG).firstOrNull()
        if (firstLimboServer != null)
            return ServerPreConnectHookResult(Optional.of(firstLimboServer))

        player.disconnect(MSG_NO_LIMBO_FOUND)
        return ServerPreConnectHookResult(Optional.empty())
    }
}

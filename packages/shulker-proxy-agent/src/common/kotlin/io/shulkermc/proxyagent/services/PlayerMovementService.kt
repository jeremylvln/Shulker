package io.shulkermc.proxyagent.services

import com.google.common.base.Preconditions
import com.google.common.base.Suppliers
import io.shulkermc.proxyagent.Configuration
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.platform.HookPostOrder
import io.shulkermc.proxyagent.platform.Player
import io.shulkermc.proxyagent.platform.PlayerPreLoginHookResult
import io.shulkermc.proxyagent.platform.ProxyPingHookResult
import io.shulkermc.proxyagent.platform.ServerPreConnectHookResult
import io.shulkermc.proxyagent.utils.createDisconnectMessage
import net.kyori.adventure.text.format.NamedTextColor
import java.net.InetSocketAddress
import java.util.Optional
import java.util.UUID

class PlayerMovementService(private val agent: ShulkerProxyAgentCommon) {
    companion object {
        private const val LOBBY_TAG = "lobby"
        private const val LIMBO_TAG = "limbo"

        private const val ONLINE_PLAYERS_COUNT_MEMOIZE_SECONDS = 10L
        private const val PLAYER_CAPACITY_COUNT_MEMOIZE_SECONDS = 60L

        private val MSG_NOT_ACCEPTING_PLAYERS =
            createDisconnectMessage(
                "Proxy is not accepting players, try reconnect.",
                NamedTextColor.RED,
            )

        private val MSG_NO_LIMBO_FOUND =
            createDisconnectMessage(
                "No limbo server found, please check your cluster configuration.",
                NamedTextColor.RED,
            )
    }

    private val maxPlayersWithExclusionDelta =
        this.agent.proxyInterface.getPlayerCapacity() - Configuration.PROXY_PLAYER_DELTA_BEFORE_EXCLUSION

    private val onlinePlayerCountSupplier =
        Suppliers.memoizeWithExpiration(
            { this.agent.cache.countOnlinePlayers() },
            ONLINE_PLAYERS_COUNT_MEMOIZE_SECONDS,
            java.util.concurrent.TimeUnit.SECONDS,
        )
    private val playerCapacityCountSupplier =
        Suppliers.memoizeWithExpiration(
            { this.agent.cache.countPlayerCapacity() },
            PLAYER_CAPACITY_COUNT_MEMOIZE_SECONDS,
            java.util.concurrent.TimeUnit.SECONDS,
        )

    private var externalClusterAddress: Optional<InetSocketAddress>
    private var isAllocatedByAgones = false
    private var acceptingPlayers = true

    init {
        this.agent.proxyInterface.addProxyPingHook(this::onProxyPing, HookPostOrder.FIRST)
        this.agent.proxyInterface.addPlayerPreLoginHook(this::onPlayerPreLogin, HookPostOrder.FIRST)
        this.agent.proxyInterface.addPlayerLoginHook(this::onPlayerLogin, HookPostOrder.EARLY)
        this.agent.proxyInterface.addPlayerDisconnectHook(this::onPlayerDisconnect, HookPostOrder.LATE)
        this.agent.proxyInterface.addServerPreConnectHook(this::onServerPreConnect, HookPostOrder.EARLY)
        this.agent.proxyInterface.addServerPostConnectHook(this::onServerPostConnect, HookPostOrder.LATE)

        if (Configuration.PROXY_PREFERRED_RECONNECT_ADDRESS.isPresent) {
            this.externalClusterAddress = Configuration.PROXY_PREFERRED_RECONNECT_ADDRESS
            this.agent.logger.info("Using preferred fleet external address: ${this.externalClusterAddress.get()}")
        } else {
            this.agent.logger.info("Proxy will watch fleet's Service to extract external address")

            this.externalClusterAddress = this.agent.kubernetesGateway.getExternalAddress()
            this.externalClusterAddress.ifPresentOrElse({ address ->
                this.agent.logger.info("Updated fleet's external address: $address")
            }, {
                this.agent.logger.warning(
                    "Fleet's external address was not found, the Service may not be ready yet or is not a LoadBalancer",
                )
            })

            this.agent.kubernetesGateway.watchExternalAddressUpdates(this::onExternalAddressUpdate)
        }
    }

    fun setAcceptingPlayers(acceptingPlayers: Boolean) {
        this.acceptingPlayers = acceptingPlayers

        if (acceptingPlayers) {
            this.agent.fileSystem.deleteReadinessLock()
            this.agent.logger.info("Proxy is now accepting players")
        } else {
            this.agent.fileSystem.createReadinessLock()
            this.agent.logger.info("Proxy is no longer accepting players")
        }
    }

    fun reconnectPlayerToCluster(playerId: UUID) {
        Preconditions.checkState(
            this.externalClusterAddress.isPresent,
            "This ProxyFleet is not operating under a LoadBalancer Service nor have a preferred address configured",
        )

        this.agent.proxyInterface.transferPlayerToAddress(playerId, this.externalClusterAddress.get())
    }

    fun reconnectEveryoneToCluster() {
        Preconditions.checkState(
            this.externalClusterAddress.isPresent,
            "This ProxyFleet is not operating under a LoadBalancer Service nor have a preferred address configured",
        )

        this.agent.proxyInterface.transferEveryoneToAddress(this.externalClusterAddress.get())
    }

    private fun onProxyPing(): ProxyPingHookResult {
        return ProxyPingHookResult(this.onlinePlayerCountSupplier.get(), this.playerCapacityCountSupplier.get())
    }

    private fun onPlayerPreLogin(): PlayerPreLoginHookResult {
        if (!this.acceptingPlayers) {
            return PlayerPreLoginHookResult.disallow(MSG_NOT_ACCEPTING_PLAYERS)
        }

        return PlayerPreLoginHookResult.allow()
    }

    private fun onPlayerLogin(player: Player) {
        this.agent.cache.updateCachedPlayerName(player.uniqueId, player.name)

        if (!this.isAllocatedByAgones) {
            this.isAllocatedByAgones = true
            this.agent.agonesGateway.setAllocated()
        }

        if (this.acceptingPlayers && this.isProxyConsideredFull()) {
            this.setAcceptingPlayers(false)
        }
    }

    private fun onPlayerDisconnect(player: Player) {
        this.agent.cache.unsetPlayerPosition(player.uniqueId)

        if (this.isAllocatedByAgones && this.agent.proxyInterface.getPlayerCount() == 0) {
            this.isAllocatedByAgones = false
            this.agent.agonesGateway.setReady()
        }

        if (!this.acceptingPlayers && !this.agent.proxyLifecycleService.isDraining() && !this.isProxyConsideredFull()) {
            this.setAcceptingPlayers(true)
        }
    }

    private fun onServerPreConnect(
        player: Player,
        originalServerName: String,
    ): ServerPreConnectHookResult {
        if (originalServerName == LOBBY_TAG) {
            val firstLobbyServer = this.agent.serverDirectoryService.getServersByTag(LOBBY_TAG).firstOrNull()
            if (firstLobbyServer != null) {
                return ServerPreConnectHookResult(true, Optional.of(firstLobbyServer))
            }

            return this.onServerPreConnect(player, LIMBO_TAG)
        }

        if (originalServerName == LIMBO_TAG) {
            val firstLimboServer = this.agent.serverDirectoryService.getServersByTag(LIMBO_TAG).firstOrNull()
            if (firstLimboServer != null) {
                return ServerPreConnectHookResult(true, Optional.of(firstLimboServer))
            }

            player.disconnect(MSG_NO_LIMBO_FOUND)
            return ServerPreConnectHookResult(false, Optional.empty())
        }

        return ServerPreConnectHookResult(true, Optional.empty())
    }

    private fun onServerPostConnect(
        player: Player,
        serverName: String,
    ) {
        this.agent.cache.setPlayerPosition(player.uniqueId, Configuration.PROXY_NAME, serverName)
    }

    private fun onExternalAddressUpdate(address: Optional<InetSocketAddress>) {
        if (address.isPresent && (this.externalClusterAddress.isEmpty || address.get() != this.externalClusterAddress.get())) {
            this.agent.logger.info("Updated fleet's external address: ${address.get()}")
        } else if (address.isEmpty && this.externalClusterAddress.isPresent) {
            this.agent.logger.warning(
                "Fleet external address was removed, transfer capabilities will be disabled",
            )
        }

        this.externalClusterAddress = address
    }

    private fun isProxyConsideredFull(): Boolean {
        return this.agent.proxyInterface.getPlayerCount() >= this.maxPlayersWithExclusionDelta
    }
}

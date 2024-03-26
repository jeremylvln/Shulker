package io.shulkermc.proxyagent.bungeecord.extensions

import io.shulkermc.proxyagent.bungeecord.ShulkerProxyAgentBungeeCord
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.lang.reflect.Method
import java.net.InetSocketAddress
import java.util.Optional
import java.util.UUID

class TransferExtension(private val plugin: ShulkerProxyAgentBungeeCord) {
    private val transferMethod: Optional<Method> = try {
        Optional.of(ProxiedPlayer::class.java.getDeclaredMethod("transfer"))
    } catch (_: NoSuchMethodException) {
        Optional.empty()
    }

    private val externalAddress: Optional<InetSocketAddress> = if (this.transferMethod.isPresent) {
        val address = this.plugin.agent.kubernetesGateway.getFleetServiceAddress()

        address.ifPresentOrElse({ addr ->
            this.plugin.agent.logger.info("Found fleet's external address: ${addr.hostName}")
        }, {
            this.plugin.agent.logger.info(
                "Fleet external address was not found, transfer capabilities will be disabled"
            )
        })

        address
    } else {
        Optional.empty()
    }

    val isSupported = this.transferMethod.isPresent && this.externalAddress.isPresent

    fun tryReconnectPlayerToCluster(playerId: UUID) {
        if (!this.isSupported) {
            throw UnsupportedOperationException()
        }

        val player = this.plugin.proxy.getPlayer(playerId) ?: return
        this.transferMethod.get().invoke(
            player,
            this.externalAddress.get().hostName,
            this.externalAddress.get().port
        )
    }

    fun tryReconnectEveryoneToCluster() {
        if (!this.isSupported) {
            throw UnsupportedOperationException()
        }

        this.plugin.proxy.players.forEach { player ->
            this.transferMethod.get().invoke(
                player,
                this.externalAddress.get().hostName,
                this.externalAddress.get().port
            )
        }
    }
}

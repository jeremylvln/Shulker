package io.shulkermc.proxyagent.velocity.extensions

import com.velocitypowered.api.proxy.Player
import io.shulkermc.proxyagent.velocity.ShulkerProxyAgentVelocity
import java.lang.reflect.Method
import java.net.InetSocketAddress
import java.util.Optional
import java.util.UUID

class TransferExtension(private val plugin: ShulkerProxyAgentVelocity) {
    private val transferMethod: Optional<Method> = try {
        Optional.of(Player::class.java.getDeclaredMethod("transferToHost"))
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

        this.plugin.proxy.getPlayer(playerId).ifPresent { player ->
            this.transferMethod.get().invoke(player, this.externalAddress.get())
        }
    }

    fun tryReconnectEveryoneToCluster() {
        if (!this.isSupported) {
            throw UnsupportedOperationException()
        }

        this.plugin.proxy.allPlayers.forEach { player ->
            this.transferMethod.get().invoke(player, this.externalAddress.get())
        }
    }
}

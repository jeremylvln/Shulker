package io.shulkermc.proxyagent.features.limbo

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon
import io.shulkermc.proxyagent.domain.Player
import io.shulkermc.proxyagent.domain.ServerPreConnectHookResult
import io.shulkermc.proxyagent.utils.createDisconnectMessage
import net.kyori.adventure.text.format.NamedTextColor
import java.util.Optional

class LimboFeature(
    private val agent: ShulkerProxyAgentCommon,
) {
    companion object {
        const val LIMBO_TAG = "limbo"

        val MSG_NO_LIMBO_FOUND = createDisconnectMessage(
            "No limbo server found, please check your cluster configuration.",
            NamedTextColor.RED)
    }
    init {
        this.agent.proxyInterface.addServerPreConnectHook { player, originalServerName ->
            this.onServerPreConnect(player, originalServerName)
        }
    }

    private fun onServerPreConnect(player: Player, originalServerName: String): ServerPreConnectHookResult {
        if (originalServerName == LIMBO_TAG) {
            val limboServers = this.agent.api.directoryAdapter.getServersByTag(LIMBO_TAG).iterator()

            if (limboServers.hasNext()) {
                val firstLimboServer = limboServers.next()
                ServerPreConnectHookResult(Optional.of(firstLimboServer))
            } else {
                player.disconnect(MSG_NO_LIMBO_FOUND)
            }
        }

        return ServerPreConnectHookResult(Optional.empty())
    }
}

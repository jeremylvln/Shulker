package io.shulkermc.proxyagent.handlers

import io.shulkermc.proxyagent.Configuration
import io.shulkermc.proxyagent.ShulkerProxyAgentCommon

class ReconnectProxyHandler(private val agent: ShulkerProxyAgentCommon) {
    fun handle(proxyName: String) {
        if (Configuration.PROXY_NAME != proxyName) {
            return
        }

        this.agent.playerMovementService.reconnectEveryoneToCluster()
    }
}

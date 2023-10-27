package io.shulkermc.proxyagent.handlers

import io.shulkermc.proxyagent.ShulkerProxyAgentCommon

class TeleportPlayerOnServerHandler(private val agent: ShulkerProxyAgentCommon) {
    fun handle(playerName: String, serverName: String) {
        this.agent.proxyInterface.teleportPlayerOnServer(playerName, serverName)
    }
}

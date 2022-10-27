package io.shulkermc.proxyagent.adapters.kubernetes

import io.shulkermc.proxyagent.adapters.kubernetes.models.ShulkerV1alpha1MinecraftServer
import io.shulkermc.proxyagent.adapters.kubernetes.models.ShulkerV1alpha1Proxy

enum class WatchAction {
    ADDED, MODIFIED, DELETED
}

interface KubernetesGatewayAdapter {
    fun destroy()

    fun emitAgentReady()
    fun emitNotAcceptingPlayers()

    fun listMinecraftServers(): ShulkerV1alpha1MinecraftServer.List

    fun watchProxyEvent(callback: (action: WatchAction, proxy: ShulkerV1alpha1Proxy) -> Unit)
    fun watchMinecraftServerEvent(callback: (action: WatchAction, minecraftServer: ShulkerV1alpha1MinecraftServer) -> Unit)
}

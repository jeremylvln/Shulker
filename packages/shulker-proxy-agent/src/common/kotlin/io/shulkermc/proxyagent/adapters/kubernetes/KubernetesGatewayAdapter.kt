package io.shulkermc.proxyagent.adapters.kubernetes

import io.shulkermc.proxyagent.adapters.kubernetes.models.AgonesV1GameServer
import java.net.InetSocketAddress
import java.util.Optional
import java.util.concurrent.CompletionStage

enum class WatchAction {
    ADDED, MODIFIED, DELETED
}

interface KubernetesGatewayAdapter {
    fun destroy()

    fun listMinecraftServers(): AgonesV1GameServer.List
    fun getFleetServiceAddress(): Optional<InetSocketAddress>

    fun watchProxyEvents(callback: (action: WatchAction, proxy: AgonesV1GameServer) -> Unit):
        CompletionStage<EventWatcher>
    fun watchMinecraftServerEvents(callback: (action: WatchAction, minecraftServer: AgonesV1GameServer) -> Unit):
        CompletionStage<EventWatcher>

    interface EventWatcher {
        fun stop()
    }
}

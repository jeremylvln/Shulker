package io.shulkermc.agent.adapters.kubernetes

import io.shulkermc.agent.adapters.kubernetes.models.AgonesV1GameServer
import io.shulkermc.agent.adapters.kubernetes.models.WatchAction
import java.net.InetSocketAddress
import java.util.Optional
import java.util.concurrent.CompletionStage

interface KubernetesGatewayAdapter {
    fun destroy()

    fun listMinecraftServers(): AgonesV1GameServer.List

    fun watchProxyEvents(
        callback: (action: WatchAction, proxy: AgonesV1GameServer) -> Unit,
    ): CompletionStage<EventWatcher>

    fun watchMinecraftServerEvents(
        callback: (action: WatchAction, minecraftServer: AgonesV1GameServer) -> Unit,
    ): CompletionStage<EventWatcher>


    interface EventWatcher {
        fun stop()
    }
}

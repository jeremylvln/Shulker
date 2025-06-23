package io.shulkermc.agent.adapters.kubernetes

import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.fabric8.kubernetes.client.informers.SharedIndexInformer
import io.fabric8.kubernetes.client.okhttp.OkHttpClientFactory
import io.shulkermc.agent.adapters.kubernetes.models.AgonesV1GameServer
import io.shulkermc.agent.adapters.kubernetes.models.WatchAction
import java.util.concurrent.CompletionStage

open class ImplKubernetesGatewayAdapter(private val namespace: String) : KubernetesGatewayAdapter {
    companion object {
        private const val PROXY_INFORMER_SYNC_MS = 30L * 1000
        private const val SERVER_INFORMER_SYNC_MS = 10L * 1000
        const val SERVICE_INFORMER_SYNC_MS = 30L * 1000
        private const val MINECRAFT_DEFAULT_PORT = 25565
    }

    protected val kubernetesClient: KubernetesClient =
        KubernetesClientBuilder()
            .withHttpClientFactory(OkHttpClientFactory())
            .build()

    private val gameServerApi =
        this.kubernetesClient.resources(
            AgonesV1GameServer::class.java,
            AgonesV1GameServer.List::class.java,
        )

    override fun destroy() {
        this.kubernetesClient.informers().stopAllRegisteredInformers()
    }

    override fun listMinecraftServers(): AgonesV1GameServer.List {
        return this.gameServerApi.inNamespace(this.namespace).withLabel(
            "app.kubernetes.io/component",
            "minecraft-server",
        ).list()
    }

    override fun watchProxyEvents(
        callback: (action: WatchAction, proxy: AgonesV1GameServer) -> Unit,
    ): CompletionStage<KubernetesGatewayAdapter.EventWatcher> {
        val eventHandler = this.createEventHandler(callback)
        val proxyInformer =
            this.gameServerApi
                .inNamespace(this.namespace)
                .withLabel("app.kubernetes.io/component", "proxy")
                .inform(eventHandler, PROXY_INFORMER_SYNC_MS)

        return this.createEventWatcher(proxyInformer)
    }

    override fun watchMinecraftServerEvents(
        callback: (action: WatchAction, minecraftServer: AgonesV1GameServer) -> Unit,
    ): CompletionStage<KubernetesGatewayAdapter.EventWatcher> {
        val eventHandler = this.createEventHandler(callback)
        val minecraftServerInformer =
            this.gameServerApi
                .inNamespace(this.namespace)
                .withLabel("app.kubernetes.io/component", "minecraft-server")
                .inform(eventHandler, SERVER_INFORMER_SYNC_MS)

        return this.createEventWatcher(minecraftServerInformer)
    }

    protected fun <T> createEventHandler(callback: (action: WatchAction, obj: T) -> Unit): ResourceEventHandler<T> {
        return object : ResourceEventHandler<T> {
            override fun onAdd(obj: T) {
                callback(WatchAction.ADDED, obj)
            }

            override fun onUpdate(
                oldObj: T,
                newObj: T,
            ) {
                callback(WatchAction.MODIFIED, newObj)
            }

            override fun onDelete(
                obj: T,
                deletedFinalStateUnknown: Boolean,
            ) {
                callback(WatchAction.DELETED, obj)
            }
        }
    }

    protected fun <T> createEventWatcher(informer: SharedIndexInformer<T>): CompletionStage<KubernetesGatewayAdapter.EventWatcher> {
        return informer.start().thenApply {
            object : KubernetesGatewayAdapter.EventWatcher {
                override fun stop() {
                    informer.stop()
                }
            }
        }
    }
}

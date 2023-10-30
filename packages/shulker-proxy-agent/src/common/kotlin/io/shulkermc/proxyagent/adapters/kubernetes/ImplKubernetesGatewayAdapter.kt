package io.shulkermc.proxyagent.adapters.kubernetes

import io.fabric8.kubernetes.api.model.ObjectReference
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.fabric8.kubernetes.client.okhttp.OkHttpClientFactory
import io.shulkermc.proxyagent.adapters.kubernetes.models.AgonesV1GameServer
import java.util.concurrent.CompletionStage

class ImplKubernetesGatewayAdapter(proxyNamespace: String, proxyName: String) : KubernetesGatewayAdapter {
    private val kubernetesClient: KubernetesClient = KubernetesClientBuilder()
        .withHttpClientFactory(OkHttpClientFactory())
        .build()

    private val gameServerApi = this.kubernetesClient.resources(
        AgonesV1GameServer::class.java,
        AgonesV1GameServer.List::class.java
    )

    private val proxyReference: ObjectReference = ObjectReferenceBuilder()
        .withApiVersion("${AgonesV1GameServer.GROUP}/${AgonesV1GameServer.VERSION}")
        .withKind(AgonesV1GameServer.KIND)
        .withNamespace(proxyNamespace)
        .withName(proxyName)
        .build()

    override fun destroy() {
        this.kubernetesClient.informers().stopAllRegisteredInformers()
    }

    override fun listMinecraftServers(): AgonesV1GameServer.List {
        return this.gameServerApi.inNamespace(this.proxyReference.namespace).withLabel(
            "app.kubernetes.io/component",
            "minecraft-server"
        ).list()
    }

    override fun watchProxyEvents(
        callback: (action: WatchAction, proxy: AgonesV1GameServer) -> Unit
    ): CompletionStage<KubernetesGatewayAdapter.EventWatcher> {
        val eventHandler = this.createEventHandler(callback)
        val proxyInformer = this.gameServerApi
            .inNamespace(this.proxyReference.namespace)
            .withLabel("app.kubernetes.io/component", "proxy")
            .inform(eventHandler, 30L * 1000)

        return proxyInformer.start()
            .thenApply {
                object : KubernetesGatewayAdapter.EventWatcher {
                    override fun stop() {
                        proxyInformer.stop()
                    }
                }
            }
    }

    override fun watchMinecraftServerEvents(
        callback: (action: WatchAction, minecraftServer: AgonesV1GameServer) -> Unit
    ): CompletionStage<KubernetesGatewayAdapter.EventWatcher> {
        val eventHandler = this.createEventHandler(callback)
        val minecraftServerInformer = this.gameServerApi
            .inNamespace(this.proxyReference.namespace)
            .withLabel("app.kubernetes.io/component", "minecraft-server")
            .inform(eventHandler, 10L * 1000)

        return minecraftServerInformer.start().thenApply {
            object : KubernetesGatewayAdapter.EventWatcher {
                override fun stop() {
                    minecraftServerInformer.stop()
                }
            }
        }
    }

    private fun <T> createEventHandler(callback: (action: WatchAction, obj: T) -> Unit): ResourceEventHandler<T> {
        return object : ResourceEventHandler<T> {
            override fun onAdd(obj: T) {
                callback(WatchAction.ADDED, obj)
            }

            override fun onUpdate(oldObj: T, newObj: T) {
                callback(WatchAction.MODIFIED, newObj)
            }

            override fun onDelete(obj: T, deletedFinalStateUnknown: Boolean) {
                callback(WatchAction.DELETED, obj)
            }
        }
    }
}

package io.shulkermc.proxyagent.adapters.kubernetes

import io.fabric8.kubernetes.api.model.ObjectReference
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.fabric8.kubernetes.client.okhttp.OkHttpClientFactory
import io.shulkermc.proxyagent.Configuration
import io.shulkermc.proxyagent.adapters.kubernetes.models.AgonesV1GameServer
import java.net.InetSocketAddress
import java.util.Optional
import java.util.concurrent.CompletionStage

class ImplKubernetesGatewayAdapter(proxyNamespace: String, proxyName: String) : KubernetesGatewayAdapter {
    companion object {
        private const val PROXY_INFORMER_SYNC_MS = 30L * 1000
        private const val SERVER_INFORMER_SYNC_MS = 10L * 1000
    }

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

    override fun getFleetServiceAddress(): Optional<InetSocketAddress> {
        val service = this.kubernetesClient.services()
            .inNamespace(Configuration.PROXY_NAMESPACE)
            .withName(Configuration.PROXY_FLEET_NAME)
            .get()

        if (service.spec.type !== "LoadBalancer") {
            return Optional.empty()
        }

        return try {
            val ingress = service.status.loadBalancer.ingress[0]
            Optional.of(InetSocketAddress(ingress.ip, ingress.ports[0].port))
        } catch (_: NullPointerException) {
            Optional.empty()
        }
    }

    override fun watchProxyEvents(
        callback: (action: WatchAction, proxy: AgonesV1GameServer) -> Unit
    ): CompletionStage<KubernetesGatewayAdapter.EventWatcher> {
        val eventHandler = this.createEventHandler(callback)
        val proxyInformer = this.gameServerApi
            .inNamespace(this.proxyReference.namespace)
            .withLabel("app.kubernetes.io/component", "proxy")
            .inform(eventHandler, PROXY_INFORMER_SYNC_MS)

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
            .inform(eventHandler, SERVER_INFORMER_SYNC_MS)

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

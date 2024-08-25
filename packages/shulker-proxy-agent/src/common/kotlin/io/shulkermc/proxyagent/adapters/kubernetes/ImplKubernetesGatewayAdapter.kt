package io.shulkermc.proxyagent.adapters.kubernetes

import io.fabric8.kubernetes.api.model.ObjectReference
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.fabric8.kubernetes.client.informers.SharedIndexInformer
import io.fabric8.kubernetes.client.okhttp.OkHttpClientFactory
import io.shulkermc.proxyagent.Configuration
import io.shulkermc.proxyagent.adapters.kubernetes.models.AgonesV1GameServer
import io.shulkermc.proxyagent.utils.addressFromHostString
import java.net.InetSocketAddress
import java.util.Optional
import java.util.concurrent.CompletionStage

class ImplKubernetesGatewayAdapter(proxyNamespace: String, proxyName: String) : KubernetesGatewayAdapter {
    companion object {
        private const val PROXY_INFORMER_SYNC_MS = 30L * 1000
        private const val SERVER_INFORMER_SYNC_MS = 10L * 1000
        private const val SERVICE_INFORMER_SYNC_MS = 30L * 1000
        private const val MINECRAFT_DEFAULT_PORT = 25565
    }

    private val kubernetesClient: KubernetesClient =
        KubernetesClientBuilder()
            .withHttpClientFactory(OkHttpClientFactory())
            .build()

    private val gameServerApi =
        this.kubernetesClient.resources(
            AgonesV1GameServer::class.java,
            AgonesV1GameServer.List::class.java,
        )

    private val proxyReference: ObjectReference =
        ObjectReferenceBuilder()
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
            "minecraft-server",
        ).list()
    }

    override fun getExternalAddress(): Optional<InetSocketAddress> {
        return this.getExternalAddressFromService(
            this.kubernetesClient.services()
                .inNamespace(Configuration.PROXY_NAMESPACE)
                .withName(Configuration.PROXY_FLEET_NAME)
                .get(),
        )
    }

    override fun watchProxyEvents(
        callback: (action: WatchAction, proxy: AgonesV1GameServer) -> Unit,
    ): CompletionStage<KubernetesGatewayAdapter.EventWatcher> {
        val eventHandler = this.createEventHandler(callback)
        val proxyInformer =
            this.gameServerApi
                .inNamespace(this.proxyReference.namespace)
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
                .inNamespace(this.proxyReference.namespace)
                .withLabel("app.kubernetes.io/component", "minecraft-server")
                .inform(eventHandler, SERVER_INFORMER_SYNC_MS)

        return this.createEventWatcher(minecraftServerInformer)
    }

    override fun watchExternalAddressUpdates(
        callback: (address: Optional<InetSocketAddress>) -> Unit,
    ): CompletionStage<KubernetesGatewayAdapter.EventWatcher> {
        val eventHandler =
            this.createEventHandler<Service> { _, service ->
                callback(this.getExternalAddressFromService(service))
            }

        val serviceInformer =
            this.kubernetesClient.services()
                .inNamespace(Configuration.PROXY_NAMESPACE)
                .withName(Configuration.PROXY_FLEET_NAME)
                .inform(eventHandler, SERVICE_INFORMER_SYNC_MS)

        return this.createEventWatcher(serviceInformer)
    }

    private fun getExternalAddressFromService(service: Service): Optional<InetSocketAddress> {
        return if (service.spec.type == "LoadBalancer") {
            Optional.ofNullable(service.status.loadBalancer?.ingress?.firstOrNull())
                .map { ingress -> addressFromHostString(ingress.ip) }
        } else {
            Optional.empty()
        }
    }

    private fun <T> createEventHandler(callback: (action: WatchAction, obj: T) -> Unit): ResourceEventHandler<T> {
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

    private fun <T> createEventWatcher(informer: SharedIndexInformer<T>): CompletionStage<KubernetesGatewayAdapter.EventWatcher> {
        return informer.start().thenApply {
            object : KubernetesGatewayAdapter.EventWatcher {
                override fun stop() {
                    informer.stop()
                }
            }
        }
    }
}

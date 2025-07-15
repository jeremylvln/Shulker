package io.shulkermc.cluster.api.adapters.kubernetes

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.fabric8.kubernetes.client.informers.SharedIndexInformer
import io.fabric8.kubernetes.client.okhttp.OkHttpClientFactory
import io.shulkermc.cluster.api.adapters.kubernetes.models.AgonesV1GameServer
import io.shulkermc.cluster.api.data.KubernetesObjectRef
import java.net.InetSocketAddress
import java.util.Optional
import java.util.concurrent.CompletionStage

class ImplKubernetesGatewayAdapter(
    private val selfReference: KubernetesObjectRef,
    private val owningFleetReference: Optional<KubernetesObjectRef>
) : KubernetesGatewayAdapter {
    companion object {
        private const val PROXY_INFORMER_SYNC_MS = 30L * 1000
        private const val SERVER_INFORMER_SYNC_MS = 10L * 1000
        private const val SERVICE_INFORMER_SYNC_MS = 30L * 1000
        private const val DEFAULT_SERVICE_PORT = 25565
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

    private var proxySide: ImplSideProxy? = null

    init {
        assert(this.selfReference.apiVersion == "${AgonesV1GameServer.GROUP}/${AgonesV1GameServer.VERSION}")
        assert(this.selfReference.kind == AgonesV1GameServer.KIND)

        if (this.owningFleetReference.isPresent) {
            assert(this.owningFleetReference.get().namespace == this.selfReference.namespace)
        }
    }

    override fun destroy() {
        this.kubernetesClient.informers().stopAllRegisteredInformers()
    }

    override fun asProxy(): KubernetesGatewayAdapter.SideProxy {
        this.proxySide = this.proxySide ?: ImplSideProxy()
        return this.proxySide!!
    }

    override fun listMinecraftServers(): AgonesV1GameServer.List {
        return this.gameServerApi.inNamespace(this.selfReference.namespace).withLabel(
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
                .inNamespace(this.selfReference.namespace)
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
                .inNamespace(this.selfReference.namespace)
                .withLabel("app.kubernetes.io/component", "minecraft-server")
                .inform(eventHandler, SERVER_INFORMER_SYNC_MS)

        return this.createEventWatcher(minecraftServerInformer)
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

    inner class ImplSideProxy : KubernetesGatewayAdapter.SideProxy {
        override fun getExternalAddress(): Optional<InetSocketAddress> {
            requireNotNull(this@ImplKubernetesGatewayAdapter.owningFleetReference)
            return this.getExternalAddressFromService(
                this@ImplKubernetesGatewayAdapter.kubernetesClient.services()
                    .inNamespace(this@ImplKubernetesGatewayAdapter.owningFleetReference.get().namespace)
                    .withName(this@ImplKubernetesGatewayAdapter.owningFleetReference.get().name)
                    .get(),
            )
        }

        override fun watchExternalAddressUpdates(
            callback: (address: Optional<InetSocketAddress>) -> Unit,
        ): CompletionStage<KubernetesGatewayAdapter.EventWatcher> {
            require(this@ImplKubernetesGatewayAdapter.owningFleetReference.isPresent)

            val eventHandler =
                this@ImplKubernetesGatewayAdapter.createEventHandler<Service> { _, service ->
                    callback(this.getExternalAddressFromService(service))
                }

            val serviceInformer =
                this@ImplKubernetesGatewayAdapter.kubernetesClient.services()
                    .inNamespace(this@ImplKubernetesGatewayAdapter.owningFleetReference.get().namespace)
                    .withName(this@ImplKubernetesGatewayAdapter.owningFleetReference.get().name)
                    .inform(eventHandler, SERVICE_INFORMER_SYNC_MS)

            return this@ImplKubernetesGatewayAdapter.createEventWatcher(serviceInformer)
        }

        private fun getExternalAddressFromService(service: Service): Optional<InetSocketAddress> {
            return if (service.spec.type == "LoadBalancer") {
                Optional.ofNullable(service.status.loadBalancer?.ingress?.firstOrNull())
                    .flatMap { ingress -> Optional.of(ingress.ip) }
                    .map { ip -> addressFromHostString(ip) }
            } else {
                Optional.empty()
            }
        }

        private fun addressFromHostString(hostAndPort: String): InetSocketAddress {
            val parts = hostAndPort.split(":")

            return if (parts.size == 2) {
                InetSocketAddress(parts[0], parts[1].toInt())
            } else {
                InetSocketAddress(parts[0], DEFAULT_SERVICE_PORT)
            }
        }
    }
}

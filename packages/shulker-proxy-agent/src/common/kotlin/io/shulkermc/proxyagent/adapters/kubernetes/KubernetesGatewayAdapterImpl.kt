package io.shulkermc.proxyagent.adapters.kubernetes

import io.fabric8.kubernetes.api.model.ObjectReference
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.fabric8.kubernetes.client.informers.ResourceEventHandler
import io.fabric8.kubernetes.client.okhttp.OkHttpClientFactory
import io.shulkermc.proxyagent.adapters.kubernetes.models.AgonesV1GameServer

class KubernetesGatewayAdapterImpl(proxyNamespace: String, proxyName: String) : KubernetesGatewayAdapter {
    private val kubernetesClient: KubernetesClient = KubernetesClientBuilder()
        .withHttpClientFactory(OkHttpClientFactory())
        .build()

    private val gameServerApi = this.kubernetesClient.resources(AgonesV1GameServer::class.java, AgonesV1GameServer.List::class.java)

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
        return this.gameServerApi.inNamespace(this.proxyReference.namespace).list()
    }

    override fun watchProxyEvents(callback: (action: WatchAction, proxy: AgonesV1GameServer) -> Unit) {
        val eventHandler = object : ResourceEventHandler<AgonesV1GameServer> {
            override fun onAdd(proxy: AgonesV1GameServer) {
                callback(WatchAction.ADDED, proxy)
            }

            override fun onUpdate(oldProxy: AgonesV1GameServer, newProxy: AgonesV1GameServer) {
                callback(WatchAction.MODIFIED, newProxy)
            }

            override fun onDelete(proxy: AgonesV1GameServer, deletedFinalStateUnknown: Boolean) {
                callback(WatchAction.DELETED, proxy)
            }
        }

        val proxyInformer = this.gameServerApi
            .inNamespace(this.proxyReference.namespace)
            .withLabel("app.kubernetes.io/component", "proxy")
            .inform(eventHandler, 30L * 1000)

        proxyInformer.start()
    }

    override fun watchMinecraftServerEvents(callback: (action: WatchAction, minecraftServer: AgonesV1GameServer) -> Unit) {
        val eventHandler = object : ResourceEventHandler<AgonesV1GameServer> {
            override fun onAdd(minecraftServer: AgonesV1GameServer) {
                callback(WatchAction.ADDED, minecraftServer)
            }

            override fun onUpdate(oldMinecraftServer: AgonesV1GameServer, newMinecraftServer: AgonesV1GameServer) {
                callback(WatchAction.MODIFIED, newMinecraftServer)
            }

            override fun onDelete(minecraftServer: AgonesV1GameServer, deletedFinalStateUnknown: Boolean) {
                callback(WatchAction.DELETED, minecraftServer)
            }
        }

        val minecraftServerInformer = this.gameServerApi
            .inNamespace(this.proxyReference.namespace)
            .withLabel("app.kubernetes.io/component", "minecraft-server")
            .inform(eventHandler, 10L * 1000)

        minecraftServerInformer.start()
    }
}

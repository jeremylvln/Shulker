package io.shulkermc.proxyagent.adapters.kubernetes

import io.fabric8.kubernetes.api.model.ObjectReference
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder
import io.fabric8.kubernetes.api.model.Service
import io.shulkermc.agent.adapters.kubernetes.ImplKubernetesGatewayAdapter
import io.shulkermc.agent.adapters.kubernetes.KubernetesGatewayAdapter
import io.shulkermc.agent.adapters.kubernetes.models.AgonesV1GameServer
import io.shulkermc.proxyagent.utils.addressFromHostString
import java.net.InetSocketAddress
import java.util.Optional
import java.util.concurrent.CompletionStage

class ProxyKubernetesGatewayAdapter(
    private val proxyNamespace: String,
    private val proxyFleet: String,
    proxyName: String
) : ImplKubernetesGatewayAdapter(
    namespace = proxyNamespace,
) {

    private val proxyReference: ObjectReference =
        ObjectReferenceBuilder()
            .withApiVersion("${AgonesV1GameServer.GROUP}/${AgonesV1GameServer.VERSION}")
            .withKind(AgonesV1GameServer.KIND)
            .withNamespace(proxyNamespace)
            .withName(proxyName)
            .build()

    override fun getExternalAddress(): Optional<InetSocketAddress> {
        return this.getExternalAddressFromService(
            this.kubernetesClient.services()
                .inNamespace(proxyNamespace)
                .withName(proxyFleet)
                .get(),
        )
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

    override fun watchExternalAddressUpdates(
        callback: (address: Optional<InetSocketAddress>) -> Unit,
    ): CompletionStage<KubernetesGatewayAdapter.EventWatcher> {
        val eventHandler =
            this.createEventHandler<Service> { _, service ->
                callback(this.getExternalAddressFromService(service))
            }

        val serviceInformer =
            this.kubernetesClient.services()
                .inNamespace(this.proxyNamespace)
                .withName(proxyFleet)
                .inform(eventHandler, SERVICE_INFORMER_SYNC_MS)

        return this.createEventWatcher(serviceInformer)
    }

}

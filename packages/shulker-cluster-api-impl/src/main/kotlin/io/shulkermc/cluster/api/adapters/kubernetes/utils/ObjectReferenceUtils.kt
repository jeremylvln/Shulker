package io.shulkermc.cluster.api.adapters.kubernetes.utils

import com.agones.dev.sdk.GameServer
import io.shulkermc.cluster.api.adapters.kubernetes.models.AgonesV1Fleet
import io.shulkermc.cluster.api.adapters.kubernetes.models.AgonesV1GameServer
import io.shulkermc.cluster.api.data.KubernetesObjectRef

fun objectRefFromGameServer(gameServer: GameServer): KubernetesObjectRef {
    return KubernetesObjectRef(
        apiVersion = "${AgonesV1GameServer.GROUP}/${AgonesV1GameServer.VERSION}",
        kind = AgonesV1GameServer.KIND,
        namespace = gameServer.objectMeta.namespace,
        name = gameServer.objectMeta.name,
    )
}

fun objectRefFromFleetName(
    fleetNamespace: String,
    fleetName: String,
): KubernetesObjectRef {
    return KubernetesObjectRef(
        apiVersion = "${AgonesV1Fleet.GROUP}/${AgonesV1Fleet.VERSION}",
        kind = AgonesV1Fleet.KIND,
        namespace = fleetNamespace,
        name = fleetName,
    )
}

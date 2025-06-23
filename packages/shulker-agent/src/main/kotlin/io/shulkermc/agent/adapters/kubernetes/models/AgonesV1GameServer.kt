package io.shulkermc.agent.adapters.kubernetes.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.DefaultKubernetesResourceList
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.model.annotation.Group
import io.fabric8.kubernetes.model.annotation.Kind
import io.fabric8.kubernetes.model.annotation.Plural
import io.fabric8.kubernetes.model.annotation.Version

@Group(AgonesV1GameServer.GROUP)
@Version(AgonesV1GameServer.VERSION)
@Kind(AgonesV1GameServer.KIND)
@Plural(AgonesV1GameServer.PLURAL)
@JsonDeserialize(using = JsonDeserializer.None::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("apiVersion", "kind", "metadata", "spec", "status")
class AgonesV1GameServer : CustomResource<AgonesV1GameServer.Spec, AgonesV1GameServer.Status>(), Namespaced {
    companion object {
        const val GROUP = "agones.dev"
        const val VERSION = "v1"
        const val KIND = "GameServer"
        const val PLURAL = "gameservers"
    }

    @JsonDeserialize(using = JsonDeserializer.None::class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Spec : KubernetesResource

    @JsonDeserialize(using = JsonDeserializer.None::class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(
        "state",
    )
    class Status : KubernetesResource {
        @set:JsonProperty("state")
        @get:JsonProperty("state")
        @JsonProperty("state")
        var state: String? = null

        fun isReady(): Boolean = this.state == "Ready" || this.state == "Reserved" || this.state == "Allocated"
    }

    @JsonDeserialize(using = JsonDeserializer.None::class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder("apiVersion", "kind", "metadata", "items")
    class List : DefaultKubernetesResourceList<AgonesV1GameServer?>()
}

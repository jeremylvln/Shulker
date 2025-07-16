package io.shulkermc.cluster.api.adapters.kubernetes.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
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

@Group(AgonesV1Fleet.GROUP)
@Version(AgonesV1Fleet.VERSION)
@Kind(AgonesV1Fleet.KIND)
@Plural(AgonesV1Fleet.PLURAL)
@JsonDeserialize(using = JsonDeserializer.None::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("apiVersion", "kind", "metadata", "spec", "status")
class AgonesV1Fleet : CustomResource<AgonesV1Fleet.Spec, AgonesV1Fleet.Status>(), Namespaced {
    companion object {
        const val GROUP = "agones.dev"
        const val VERSION = "v1"
        const val KIND = "Fleet"
        const val PLURAL = "fleets"
    }

    @JsonDeserialize(using = JsonDeserializer.None::class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Spec : KubernetesResource

    @JsonDeserialize(using = JsonDeserializer.None::class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Status : KubernetesResource

    @JsonDeserialize(using = JsonDeserializer.None::class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder("apiVersion", "kind", "metadata", "items")
    class List : DefaultKubernetesResourceList<AgonesV1Fleet?>()
}

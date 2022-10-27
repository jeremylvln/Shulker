package io.shulkermc.proxyagent.adapters.kubernetes.models

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

@Group(ShulkerV1alpha1Proxy.GROUP)
@Version(ShulkerV1alpha1Proxy.VERSION)
@Kind(ShulkerV1alpha1Proxy.KIND)
@Plural(ShulkerV1alpha1Proxy.PLURAL)
@JsonDeserialize(using = JsonDeserializer.None::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("apiVersion", "kind", "metadata", "spec")
class ShulkerV1alpha1Proxy : CustomResource<ShulkerV1alpha1Proxy.Spec, Void>(), Namespaced {
    companion object {
        const val GROUP = "shulkermc.io"
        const val VERSION = "v1alpha1"
        const val KIND = "Proxy"
        const val PLURAL = "proxies"
    }

    @JsonDeserialize(using = JsonDeserializer.None::class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Spec : KubernetesResource

    @JsonDeserialize(using = JsonDeserializer.None::class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder("apiVersion", "kind", "metadata", "items")
    class List : DefaultKubernetesResourceList<ShulkerV1alpha1Proxy?>()
}

package io.shulkermc.proxyagent.adapters.kubernetes.models

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.model.annotation.Group
import io.fabric8.kubernetes.model.annotation.Kind
import io.fabric8.kubernetes.model.annotation.Plural
import io.fabric8.kubernetes.model.annotation.Version
import java.util.*

@Group(ShulkerV1alpha1MinecraftServer.GROUP)
@Version(ShulkerV1alpha1MinecraftServer.VERSION)
@Kind(ShulkerV1alpha1MinecraftServer.KIND)
@Plural(ShulkerV1alpha1MinecraftServer.PLURAL)
@JsonDeserialize(using = JsonDeserializer.None::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("apiVersion", "kind", "metadata", "spec", "status")
class ShulkerV1alpha1MinecraftServer : CustomResource<ShulkerV1alpha1MinecraftServer.Spec, ShulkerV1alpha1MinecraftServer.Status>(), Namespaced {
    companion object {
        const val GROUP = "shulkermc.io"
        const val VERSION = "v1alpha1"
        const val KIND = "MinecraftServer"
        const val PLURAL = "minecraftservers"
    }

    @JsonDeserialize(using = JsonDeserializer.None::class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder("tags")
    class Spec : KubernetesResource {
        @set:JsonProperty("tags")
        @get:JsonProperty("tags")
        @JsonProperty("tags")
        var tags: kotlin.collections.List<String>? = null
    }

    @JsonDeserialize(using = JsonDeserializer.None::class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(
        "conditions",
        "serverIP"
    )
    class Status : KubernetesResource {
        @set:JsonProperty("conditions")
        @get:JsonProperty("conditions")
        @JsonProperty("conditions")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        var conditions: kotlin.collections.List<Condition>? = null

        @set:JsonProperty("serverIP")
        @get:JsonProperty("serverIP")
        @JsonProperty("serverIP")
        var serverIP: String? = null

        fun getConditionByType(type: String): Optional<Condition> {
            if (this.conditions == null)
                return Optional.empty()

            for (condition in this.conditions!!)
                if (condition.type == type)
                    return Optional.of(condition)

            return Optional.empty()
        }

        class Condition {
            @get:JsonProperty("type")
            @set:JsonProperty("type")
            @JsonProperty("type")
            var type: String? = null

            @get:JsonProperty("status")
            @set:JsonProperty("status")
            @JsonProperty("status")
            var status: String? = null

            @get:JsonProperty("observedGeneration")
            @set:JsonProperty("observedGeneration")
            @JsonProperty("observedGeneration")
            var observedGeneration: Long? = null

            @get:JsonProperty("lastTransitionTime")
            @set:JsonProperty("lastTransitionTime")
            @JsonProperty("lastTransitionTime")
            var lastTransitionTime: String? = null

            @get:JsonProperty("reason")
            @set:JsonProperty("reason")
            @JsonProperty("reason")
            var reason: String? = null

            @get:JsonProperty("message")
            @set:JsonProperty("message")
            @JsonProperty("message")
            var message: String? = null
        }
    }

    @JsonDeserialize(using = JsonDeserializer.None::class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder("apiVersion", "kind", "metadata", "items")
    class List : DefaultKubernetesResourceList<ShulkerV1alpha1MinecraftServer?>()
}

package io.shulkermc.cluster.api.data

data class KubernetesObjectRef(
    val apiVersion: String,
    val kind: String,
    val namespace: String?,
    val name: String,
) {
    override fun toString(): String = "${this.namespace ?: "*"}/${this.name}"
}

use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

#[derive(CustomResource, Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[kube(
    kind = "MinecraftCluster",
    group = "shulkermc.io",
    version = "v1alpha1",
    namespaced,
    status = "MinecraftClusterStatus",
    printcolumn = r#"{"name": "Age", "type": "date", "jsonPath": ".metadata.creationTimestamp"}"#
)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftClusterSpec {}

/// The status object of `MinecraftCluster`
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftClusterStatus {}

/// MinecraftClusterRef is to be used on resources referencing
/// a MinecraftCluster.
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftClusterRef {
    /// Name of the Kubernetes `MinecraftCluster` owning
    /// this resource
    pub name: String,
}

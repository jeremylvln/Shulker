use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};
use strum::{Display, IntoStaticStr};

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
pub struct MinecraftClusterSpec {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub redis: Option<MinecraftClusterRedisSpec>,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftClusterRedisSpec {
    pub type_: MinecraftClusterRedisDeploymentType,
}

#[derive(
    PartialEq, Deserialize, Serialize, Clone, Debug, Default, JsonSchema, IntoStaticStr, Display,
)]
pub enum MinecraftClusterRedisDeploymentType {
    #[default]
    ManagedSingleNode,
    Provided,
}

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

impl MinecraftClusterRef {
    /// Creates a new `MinecraftClusterRef` with the given name
    pub fn new(name: impl Into<String>) -> Self {
        Self { name: name.into() }
    }
}

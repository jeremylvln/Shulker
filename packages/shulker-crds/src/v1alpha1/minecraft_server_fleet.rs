use k8s_openapi::apimachinery::pkg::apis::meta::v1::Condition;
use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

use super::{minecraft_cluster::MinecraftClusterRef, minecraft_server::MinecraftServerSpec};

use crate::{
    condition::HasConditions,
    schemas::{FleetAutoscalingSpec, TemplateSpec},
};

#[derive(CustomResource, Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[kube(
    kind = "MinecraftServerFleet",
    group = "shulkermc.io",
    version = "v1alpha1",
    namespaced,
    status = "MinecraftServerFleetStatus",
    printcolumn = r#"{"name": "Replicas", "type": "integer", "jsonPath": ".status.replicas"}, {"name": "Age", "type": "date", "jsonPath": ".metadata.creationTimestamp"}"#
)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerFleetSpec {
    /// Reference to a `MinecraftCluster`. Adding this will enroll
    /// this `MinecraftServerFleet` to be part of a `MinecraftCluster`
    pub cluster_ref: MinecraftClusterRef,

    /// Number of Proxy replicas to create
    #[schemars(default = "MinecraftServerFleetSpec::default_replicas")]
    pub replicas: u32,

    /// Describe how to create the underlying `MinecraftServers`
    pub template: TemplateSpec<MinecraftServerSpec>,

    /// Autoscaling configuration for this `MinecraftServerFleet`.
    pub autoscaling: Option<FleetAutoscalingSpec>,
}

impl MinecraftServerFleetSpec {
    fn default_replicas() -> u32 {
        1
    }
}

/// The status object of `MinecraftServerFleet`
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerFleetStatus {
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub conditions: Vec<Condition>,
    pub replicas: i32,
    pub ready_replicas: i32,
    pub allocated_replicas: i32,
}

impl HasConditions for MinecraftServerFleetStatus {
    fn conditions(&self) -> &Vec<Condition> {
        &self.conditions
    }

    fn conditions_mut(&mut self) -> &mut Vec<Condition> {
        &mut self.conditions
    }
}

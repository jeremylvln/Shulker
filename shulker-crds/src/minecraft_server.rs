use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

use chrono::{DateTime, Utc};

/// Describe a MinecraftServer resource, aka.
/// a bridge between a MinecraftServerTemplate
/// and a Kubernetes Deployment.
/// 
/// This means that the operator will, necessarily,
/// create a Kubernetes Deployment for each
/// MinecraftServer resource found.
#[derive(CustomResource, Clone, Debug, Deserialize, Serialize, JsonSchema)]
#[kube(
    group = "shulker.io",
    version = "v1beta1",
    kind = "MinecraftServer",
    namespaced
)]
#[kube(status = "MinecraftServerStatus")]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerSpec {
    /// Root template.
    pub template: String,
}

/// Struct describing the status of a MinecraftServer
/// resource.
/// 
/// Will be automatically patched by the operator.
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerStatus {
    /// Status conditions.
    pub conditions: Vec<MinecraftServerStatusCondition>,
    /// Number of players playing.
    pub players: i32,
}

/// Struct describing one status condition of the
/// statys of a MinecraftServer resource.
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerStatusCondition {
    /// Timestamp of the last change of the condition.
    pub last_transition_time: DateTime<Utc>,
    /// Human-readable message describing the condition's
    /// last transition.
    pub message: Option<String>,
    /// Brief machine-readable explanation of the condition's
    /// last transition.
    pub reason: Option<String>,
    /// Status of the condition (either True or False).
    pub status: String,
    /// Type of the condition (must only be Ready).
    pub r#type: String,
}

use kube::CustomResource;
use serde::{Deserialize, Serialize};

use chrono::{DateTime, Utc};

#[derive(CustomResource, Clone, Debug, Deserialize, Serialize)]
#[kube(
    group = "shulker.io",
    version = "v1beta1",
    kind = "MinecraftServer",
    namespaced
)]
#[kube(status = "MinecraftServerStatus")]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerSpec {
    pub template: String,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerStatus {
    pub conditions: Vec<MinecraftServerStatusCondition>,
    pub players: i32,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerStatusCondition {
    pub last_transition_time: DateTime<Utc>,
    pub message: Option<String>,
    pub reason: Option<String>,
    pub status: String,
    pub r#type: String,
}

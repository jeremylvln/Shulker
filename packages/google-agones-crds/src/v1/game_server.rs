use k8s_openapi::api::core::v1::PodTemplateSpec;
use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

#[derive(CustomResource, Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[kube(
    kind = "GameServer",
    group = "agones.dev",
    version = "v1",
    namespaced,
    status = "GameServerStatus"
)]
#[serde(rename_all = "camelCase")]
pub struct GameServerSpec {
    pub ports: Option<Vec<GameServerPortSpec>>,
    pub health: Option<GameServerHealthSpec>,
    pub template: PodTemplateSpec,
    pub eviction: Option<GameServerEvictionSpec>,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct GameServerPortSpec {
    pub name: String,
    pub container_port: i32,
    pub protocol: String,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct GameServerHealthSpec {
    pub disabled: Option<bool>,
    pub period_seconds: Option<i32>,
    pub failure_threshold: Option<i32>,
    pub initial_delay_seconds: Option<i32>,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct GameServerEvictionSpec {
    pub safe: String,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct GameServerStatus {
    pub state: String,
    pub address: String,
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub ports: Vec<GameServerStatusPort>,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
pub struct GameServerStatusPort {
    pub name: String,
    pub port: i32,
}

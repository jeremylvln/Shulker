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
    #[serde(skip_serializing_if = "Option::is_none")]
    pub ports: Option<Vec<GameServerPortSpec>>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub health: Option<GameServerHealthSpec>,
    pub template: PodTemplateSpec,
    #[serde(skip_serializing_if = "Option::is_none")]
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
    /// Specify whether the health check should be disabled.
    #[serde(skip_serializing_if = "Option::is_none")]
    pub disabled: Option<bool>,

    /// Specify the period in seconds between health checks.
    #[serde(skip_serializing_if = "Option::is_none")]
    pub period_seconds: Option<i32>,

    /// Specify how much health failure is tolerated before the GameServer is considered unhealthy.
    #[serde(skip_serializing_if = "Option::is_none")]
    pub failure_threshold: Option<i32>,

    /// Specify the initial delay in seconds before the first health check is performed.
    #[serde(skip_serializing_if = "Option::is_none")]
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

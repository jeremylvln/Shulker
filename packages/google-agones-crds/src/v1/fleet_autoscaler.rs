use k8s_openapi::{
    api::admissionregistration::v1::WebhookClientConfig,
    apimachinery::pkg::util::intstr::IntOrString,
};
use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};
use strum::{Display, IntoStaticStr};

#[derive(CustomResource, Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[kube(
    kind = "FleetAutoscaler",
    group = "autoscaling.agones.dev",
    version = "v1",
    namespaced,
    status = "FleetAutoscalerStatus"
)]
#[serde(rename_all = "camelCase")]
pub struct FleetAutoscalerSpec {
    pub fleet_name: String,
    pub policy: FleetAutoscalerPolicySpec,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct FleetAutoscalerPolicySpec {
    #[serde(default)]
    pub type_: FleetAutoscalerPolicy,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub buffer: Option<FleetAutoscalerPolicyBufferSpec>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub webhook: Option<WebhookClientConfig>,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default, IntoStaticStr, Display)]
pub enum FleetAutoscalerPolicy {
    #[default]
    Buffer,
    Webhook,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct FleetAutoscalerPolicyBufferSpec {
    pub max_replicas: i32,
    pub min_replicas: i32,
    pub buffer_size: IntOrString,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct FleetAutoscalerStatus {
    pub current_replicas: i32,
    pub desired_replicas: i32,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub last_scale_time: Option<k8s_openapi::apimachinery::pkg::apis::meta::v1::Time>,
    pub able_to_scale: bool,
    pub scaling_limited: bool,
}

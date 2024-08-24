use std::collections::BTreeMap;

use k8s_openapi::apimachinery::pkg::apis::meta::v1::Condition;
use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};
use strum::{Display, IntoStaticStr};

use super::minecraft_cluster::MinecraftClusterRef;

use crate::{
    condition::HasConditions,
    resourceref::ResourceRefSpec,
    schemas::{FleetAutoscalingSpec, ImageOverrideSpec, TemplateSpec},
};

#[derive(CustomResource, Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[kube(
    kind = "ProxyFleet",
    group = "shulkermc.io",
    version = "v1alpha1",
    namespaced,
    status = "ProxyFleetStatus",
    scale = r#"{"specReplicasPath": ".spec.replicas", "statusReplicasPath": ".status.replicas"}"#,
    printcolumn = r#"{"name": "Replicas", "type": "integer", "jsonPath": ".status.replicas"}, {"name": "Age", "type": "date", "jsonPath": ".metadata.creationTimestamp"}"#
)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetSpec {
    /// Reference to a `MinecraftCluster`. Adding this will enroll
    /// this `ProxyFleet` to be part of a `MinecraftCluster`
    pub cluster_ref: MinecraftClusterRef,

    /// Number of Proxy replicas to create
    #[schemars(default = "ProxyFleetSpec::default_replicas")]
    pub replicas: u32,

    /// Describe how to create the underlying `Proxies`
    pub template: TemplateSpec<ProxyFleetTemplateSpec>,

    /// The desired state of the Kubernetes `Service` to create for the
    /// Proxy Deployment
    #[serde(skip_serializing_if = "Option::is_none")]
    pub service: Option<ProxyFleetServiceSpec>,

    /// Autoscaling configuration for this `ProxyFleet`
    #[serde(skip_serializing_if = "Option::is_none")]
    pub autoscaling: Option<FleetAutoscalingSpec>,
}

#[cfg(not(tarpaulin_include))]
impl ProxyFleetSpec {
    fn default_replicas() -> u32 {
        1
    }
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetTemplateSpec {
    /// Defines the version of the proxy to run.
    /// The version can come from a channel which allows the user
    /// to run a version different from the default BungeeCord
    pub version: ProxyFleetTemplateVersionSpec,

    /// Custom configuration flags to custom the proxy behavior
    pub config: ProxyFleetTemplateConfigurationSpec,

    /// Overrides for values to be injected in the created `Pod`
    /// of this `ProxyFleet`
    #[serde(skip_serializing_if = "Option::is_none")]
    pub pod_overrides: Option<ProxyFleetTemplatePodOverridesSpec>,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetTemplateVersionSpec {
    /// Channel of the version to use. Defaults to Velocity
    #[serde(default)]
    pub channel: ProxyFleetTemplateVersion,

    /// Name of the version to use
    pub name: String,
}

#[derive(
    PartialEq, Deserialize, Serialize, Clone, Debug, Default, JsonSchema, IntoStaticStr, Display,
)]
pub enum ProxyFleetTemplateVersion {
    #[default]
    Velocity,
    BungeeCord,
    Waterfall,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetTemplateConfigurationSpec {
    /// Name of an optional ConfigMap already containing the proxy
    /// configuration
    #[serde(skip_serializing_if = "Option::is_none")]
    pub existing_config_map_name: Option<String>,

    /// List of references to plugins to download
    #[serde(skip_serializing_if = "Option::is_none")]
    pub plugins: Option<Vec<ResourceRefSpec>>,

    /// List of optional references to patch archives to download
    /// and extract at the root of the proxy. Gzippied tarballs only
    #[serde(skip_serializing_if = "Option::is_none")]
    pub patches: Option<Vec<ResourceRefSpec>>,

    /// Number of maximum players that can connect to the
    /// ProxyFleet Deployment
    #[schemars(default = "ProxyFleetTemplateConfigurationSpec::default_max_players")]
    pub max_players: u32,

    /// Message to display when the players query the status
    /// of the ProxyFleet Deployment
    #[schemars(default = "ProxyFleetTemplateConfigurationSpec::default_motd")]
    pub motd: String,

    /// Server icon image in base64 format
    #[schemars(default = "ProxyFleetTemplateConfigurationSpec::default_server_icon")]
    pub server_icon: String,

    /// Whether to enable the PROXY protocol
    #[schemars(default = "ProxyFleetTemplateConfigurationSpec::default_proxy_protocol")]
    pub proxy_protocol: bool,

    /// Number of seconds the proxy will live before being
    /// drained automatically
    #[schemars(default = "ProxyFleetTemplateConfigurationSpec::default_ttl_seconds")]
    pub ttl_seconds: u32,

    /// Number of player slots to reserve when exclusing a proxy
    /// from the load balancer. This will allow load balancer
    /// implementations to update itself while still being able
    /// to accept some players
    #[schemars(
        default = "ProxyFleetTemplateConfigurationSpec::default_players_delta_before_exclusion"
    )]
    pub players_delta_before_exclusion: u32,

    /// Whether to skip downloading the agent plugin on Pod startup. It way
    /// be useful if you are already building your own image with the agent
    /// plugin bundled
    #[schemars(default = "ProxyFleetTemplateConfigurationSpec::default_skip_agent_download")]
    pub skip_agent_download: bool,
}

#[cfg(not(tarpaulin_include))]
impl ProxyFleetTemplateConfigurationSpec {
    fn default_max_players() -> u32 {
        100
    }

    fn default_motd() -> String {
        "A Minecraft Cluster on Shulker".to_string()
    }

    fn default_server_icon() -> String {
        include_str!(concat!(env!("OUT_DIR"), "/default-server-icon.txt")).to_string()
    }

    fn default_proxy_protocol() -> bool {
        false
    }

    fn default_ttl_seconds() -> u32 {
        86400
    }

    fn default_players_delta_before_exclusion() -> u32 {
        15
    }

    fn default_skip_agent_download() -> bool {
        false
    }
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetTemplatePodOverridesSpec {
    /// Image to use as replacement for the built-in one
    #[serde(skip_serializing_if = "Option::is_none")]
    pub image: Option<ImageOverrideSpec>,

    /// Extra environment variables to add to the crated `Pod`
    #[serde(skip_serializing_if = "Option::is_none")]
    pub env: Option<Vec<k8s_openapi::api::core::v1::EnvVar>>,

    /// The desired compute resource requirements of the created `Pod`
    #[serde(skip_serializing_if = "Option::is_none")]
    pub resources: Option<k8s_openapi::api::core::v1::ResourceRequirements>,

    /// Affinity scheduling rules to be applied on created `Pod`
    #[serde(skip_serializing_if = "Option::is_none")]
    pub affinity: Option<k8s_openapi::api::core::v1::Affinity>,

    /// Node selector to be applied on created `Pod`
    #[serde(skip_serializing_if = "Option::is_none")]
    pub node_selector: Option<BTreeMap<String, String>>,

    /// Tolerations to be applied on created `Pod`
    #[serde(skip_serializing_if = "Option::is_none")]
    pub tolerations: Option<Vec<k8s_openapi::api::core::v1::Toleration>>,

    /// Name of the ServiceAccount to use
    #[serde(skip_serializing_if = "Option::is_none")]
    pub service_account_name: Option<String>,

    /// Extra volumesmounts to add to the created `Pod`
    #[serde(skip_serializing_if = "Option::is_none")]
    pub volume_mounts: Option<Vec<k8s_openapi::api::core::v1::VolumeMount>>,

    /// Extra volumes to add to the created `Pod`
    #[serde(skip_serializing_if = "Option::is_none")]
    pub volumes: Option<Vec<k8s_openapi::api::core::v1::Volume>>,

    /// Extra ports to add to the created `Pod`'s main container
    #[serde(skip_serializing_if = "Option::is_none")]
    pub ports: Option<Vec<k8s_openapi::api::core::v1::ContainerPort>>,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetServiceSpec {
    /// Type of Service to create
    /// Must be one of: ClusterIP, LoadBalancer, NodePort
    #[serde(default)]
    pub type_: ProxyFleetServiceType,

    // Annotations to add to the `Service`
    #[serde(skip_serializing_if = "Option::is_none")]
    pub annotations: Option<BTreeMap<String, String>>,

    // Describe how nodes distribute service traffic to the proxy
    // #[schemars(schema_with = "ProxyFleetServiceSpec::schema_external_traffic_policy")]
    #[serde(skip_serializing_if = "Option::is_none")]
    pub external_traffic_policy: Option<ProxyFleetServiceExternalTrafficPolicy>,

    // An alternative address that should be used internally that
    // can be different that the one provided by Kubernetes
    #[serde(skip_serializing_if = "Option::is_none")]
    pub preferred_reconnection_address: Option<String>,
}

#[derive(
    PartialEq, Deserialize, Serialize, Clone, Debug, Default, JsonSchema, IntoStaticStr, Display,
)]
pub enum ProxyFleetServiceType {
    ClusterIP,
    NodePort,
    #[default]
    LoadBalancer,
}

#[derive(
    PartialEq, Deserialize, Serialize, Clone, Debug, Default, JsonSchema, IntoStaticStr, Display,
)]
pub enum ProxyFleetServiceExternalTrafficPolicy {
    Cluster,
    #[default]
    Local,
}

/// The status object of `ProxyFleet`
#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetStatus {
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub conditions: Vec<Condition>,
    pub replicas: i32,
    pub ready_replicas: i32,
    pub allocated_replicas: i32,
}

#[cfg(not(tarpaulin_include))]
impl HasConditions for ProxyFleetStatus {
    fn conditions(&self) -> &Vec<Condition> {
        &self.conditions
    }

    fn conditions_mut(&mut self) -> &mut Vec<Condition> {
        &mut self.conditions
    }
}

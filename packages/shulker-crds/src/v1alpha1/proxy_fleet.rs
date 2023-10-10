use std::collections::{BTreeMap, HashMap};

use k8s_openapi::apimachinery::pkg::apis::meta::v1::Condition;
use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

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
    shortname = "skrpf",
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
    pub service: Option<ProxyFleetServiceSpec>,

    /// Autoscaling configuration for this `ProxyFleet`.
    pub autoscaling: Option<FleetAutoscalingSpec>,
}

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
    pub pod_overrides: Option<ProxyFleetTemplatePodOverridesSpec>,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetTemplateVersionSpec {
    /// Channel of the version to use. Defaults to Velocity
    #[schemars(default = "ProxyFleetTemplateVersionSpec::default_channel")]
    #[schemars(schema_with = "ProxyFleetTemplateVersionSpec::schema_channel")]
    pub channel: String,

    /// Name of the version to use
    pub name: String,
}

impl ProxyFleetTemplateVersionSpec {
    fn default_channel() -> String {
        "Velocity".to_string()
    }

    fn schema_channel(_: &mut schemars::gen::SchemaGenerator) -> schemars::schema::Schema {
        schemars::schema::SchemaObject {
            instance_type: Some(schemars::schema::InstanceType::String.into()),
            enum_values: Some(vec![
                serde_json::Value::String("Velocity".to_string()),
                serde_json::Value::String("BungeeCord".to_string()),
                serde_json::Value::String("Waterfall".to_string()),
            ]),
            ..Default::default()
        }
        .into()
    }
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetTemplateConfigurationSpec {
    /// Name of an optional ConfigMap already containing the proxy
    /// configuration
    pub existing_config_map_name: Option<String>,

    /// List of references to plugins to download
    pub plugins: Option<Vec<ResourceRefSpec>>,

    /// List of optional references to patch archives to download
    /// and extract at the root of the proxy. Gzippied tarballs only
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
}

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
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetTemplatePodOverridesSpec {
    /// Image to use as replacement for the built-in one
    pub image: Option<ImageOverrideSpec>,

    /// Extra environment variables to add to the crated `Pod`
    pub env: Option<Vec<k8s_openapi::api::core::v1::EnvVar>>,

    /// The desired compute resource requirements of the created `Pod`
    pub resources: Option<k8s_openapi::api::core::v1::ResourceRequirements>,

    /// Affinity scheduling rules to be applied on created `Pod`
    pub affinity: Option<k8s_openapi::api::core::v1::Affinity>,

    /// Node selector to be applied on created `Pod`
    pub node_selector: Option<HashMap<String, String>>,

    /// Tolerations to be applied on created `Pod`
    pub tolerations: Option<Vec<k8s_openapi::api::core::v1::Toleration>>,

    /// Name of the ServiceAccount to use
    pub service_account_name: Option<String>,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetServiceSpec {
    /// Type of Service to create.
    /// Must be one of: ClusterIP, LoadBalancer, NodePort
    #[schemars(default = "ProxyFleetServiceSpec::default_type")]
    #[schemars(schema_with = "ProxyFleetServiceSpec::schema_type")]
    pub type_: String,

    // Annotations to add to the `Service`
    pub annotations: Option<BTreeMap<String, String>>,

    // Describe how nodes distribute service traffic to the proxy.
    #[schemars(default = "ProxyFleetServiceSpec::default_external_traffic_policy")]
    #[schemars(schema_with = "ProxyFleetServiceSpec::schema_external_traffic_policy")]
    pub external_traffic_policy: String,
}

impl ProxyFleetServiceSpec {
    fn default_type() -> String {
        "LoadBalancer".to_string()
    }

    fn schema_type(_: &mut schemars::gen::SchemaGenerator) -> schemars::schema::Schema {
        schemars::schema::SchemaObject {
            instance_type: Some(schemars::schema::InstanceType::String.into()),
            enum_values: Some(vec![
                serde_json::Value::String("ClusterIP".to_string()),
                serde_json::Value::String("NodePort".to_string()),
                serde_json::Value::String("LoadBalancer".to_string()),
            ]),
            ..Default::default()
        }
        .into()
    }

    fn default_external_traffic_policy() -> String {
        "Cluster".to_string()
    }

    fn schema_external_traffic_policy(
        _: &mut schemars::gen::SchemaGenerator,
    ) -> schemars::schema::Schema {
        schemars::schema::SchemaObject {
            instance_type: Some(schemars::schema::InstanceType::String.into()),
            enum_values: Some(vec![
                serde_json::Value::String("Cluster".to_string()),
                serde_json::Value::String("Local".to_string()),
            ]),
            ..Default::default()
        }
        .into()
    }
}

/// The status object of `ProxyFleet`
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct ProxyFleetStatus {
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub conditions: Vec<Condition>,
    pub replicas: i32,
    pub ready_replicas: i32,
    pub allocated_replicas: i32,
}

impl HasConditions for ProxyFleetStatus {
    fn conditions(&self) -> &Vec<Condition> {
        &self.conditions
    }

    fn conditions_mut(&mut self) -> &mut Vec<Condition> {
        &mut self.conditions
    }
}

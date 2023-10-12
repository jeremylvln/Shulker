use std::collections::HashMap;

use k8s_openapi::apimachinery::pkg::apis::meta::v1::Condition;
use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

use super::minecraft_cluster::MinecraftClusterRef;

use crate::{condition::HasConditions, resourceref::ResourceRefSpec, schemas::ImageOverrideSpec};

#[derive(CustomResource, Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[kube(
    kind = "MinecraftServer",
    group = "shulkermc.io",
    version = "v1alpha1",
    namespaced,
    status = "MinecraftServerStatus",
    printcolumn = r#"{"name": "Ready", "type": "boolean", "jsonPath": ".status.conditions[?(@.type==\"Ready\")].status"}, {"name": "Phase", "type": "string", "jsonPath": ".status.conditions[?(@.type==\"Phase\")].reason"}, {"name": "Age", "type": "date", "jsonPath": ".metadata.creationTimestamp"}"#
)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerSpec {
    /// Reference to a `MinecraftCluster`. Adding this will enroll
    /// this `MinecraftServer` to be part of a `MinecraftCluster`
    pub cluster_ref: MinecraftClusterRef,

    /// List of tags to associate to the `MinecraftServer`
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub tags: Vec<String>,

    /// Defines the version of the server to run.
    /// The version can come from a channel which allows the user
    /// to run a version different from the default BungeeCord
    pub version: MinecraftServerVersionSpec,

    /// Custom configuration flags to custom the server behavior
    pub config: MinecraftServerConfigurationSpec,

    /// Overrides for values to be injected in the created `Pod`
    /// of this `MinecraftServer`
    pub pod_overrides: Option<MinecraftServerPodOverridesSpec>,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerVersionSpec {
    /// Channel of the version to use. Defaults to Paper
    #[schemars(default = "MinecraftServerVersionSpec::default_channel")]
    #[schemars(schema_with = "MinecraftServerVersionSpec::schema_channel")]
    pub channel: String,

    /// Name of the version to use
    pub name: String,
}

#[cfg(not(tarpaulin_include))]
impl MinecraftServerVersionSpec {
    fn default_channel() -> String {
        "Paper".to_string()
    }

    fn schema_channel(_: &mut schemars::gen::SchemaGenerator) -> schemars::schema::Schema {
        schemars::schema::SchemaObject {
            instance_type: Some(schemars::schema::InstanceType::String.into()),
            enum_values: Some(vec![
                serde_json::Value::String("Paper".to_string()),
                serde_json::Value::String("Bukkit".to_string()),
                serde_json::Value::String("Spigot".to_string()),
            ]),
            ..Default::default()
        }
        .into()
    }
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerConfigurationSpec {
    /// Name of an optional ConfigMap already containing the server
    /// configuration
    pub existing_config_map_name: Option<String>,

    /// Reference to a world to download and extract. Gzipped tarball
    /// only
    pub world: Option<ResourceRefSpec>,

    /// List of references to plugins to download
    pub plugins: Option<Vec<ResourceRefSpec>>,

    /// List of optional references to patch archives to download
    /// and extract at the root of the server. Gzippied tarballs only
    pub patches: Option<Vec<ResourceRefSpec>>,

    /// Number of maximum players that can connect to the
    /// MinecraftServer Deployment
    #[schemars(default = "MinecraftServerConfigurationSpec::default_max_players")]
    pub max_players: u32,

    /// Whether to allow the MinecraftServer to generate a Nether world
    /// and the players to enter it
    #[schemars(default = "MinecraftServerConfigurationSpec::default_disable_nether")]
    pub disable_nether: bool,

    /// Whether to allow the MinecraftServer to generate a End world
    /// and the players to enter it
    #[schemars(default = "MinecraftServerConfigurationSpec::default_disable_end")]
    pub disable_end: bool,

    /// Custom properties to set inside the server.properties file of
    /// the Pod. Note: Shulker may override some values
    #[serde(default, skip_serializing_if = "HashMap::is_empty")]
    pub server_properties: HashMap<String, String>,

    /// Type of forwarding the proxies are using between themselves and
    /// this `MinecraftServer`
    #[schemars(default = "MinecraftServerConfigurationSpec::default_proxy_forwarding_mode")]
    #[schemars(schema_with = "MinecraftServerConfigurationSpec::schema_proxy_forwarding_mode")]
    pub proxy_forwarding_mode: String,
}

#[cfg(not(tarpaulin_include))]
impl MinecraftServerConfigurationSpec {
    fn default_max_players() -> u32 {
        20
    }

    fn default_disable_nether() -> bool {
        true
    }

    fn default_disable_end() -> bool {
        true
    }

    fn default_proxy_forwarding_mode() -> String {
        "Velocity".to_string()
    }

    fn schema_proxy_forwarding_mode(
        _: &mut schemars::gen::SchemaGenerator,
    ) -> schemars::schema::Schema {
        schemars::schema::SchemaObject {
            instance_type: Some(schemars::schema::InstanceType::String.into()),
            enum_values: Some(vec![
                serde_json::Value::String("BungeeCord".to_string()),
                serde_json::Value::String("Velocity".to_string()),
            ]),
            ..Default::default()
        }
        .into()
    }
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerPodOverridesSpec {
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

/// The status object of `MinecraftServer`
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerStatus {
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub conditions: Vec<Condition>,
    pub address: String,
    pub port: i32,
}

#[cfg(not(tarpaulin_include))]
impl HasConditions for MinecraftServerStatus {
    fn conditions(&self) -> &Vec<Condition> {
        &self.conditions
    }

    fn conditions_mut(&mut self) -> &mut Vec<Condition> {
        &mut self.conditions
    }
}

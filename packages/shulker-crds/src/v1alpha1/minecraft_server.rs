use std::collections::BTreeMap;

use k8s_openapi::apimachinery::pkg::apis::meta::v1::Condition;
use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};
use strum::{Display, IntoStaticStr};

use super::minecraft_cluster::MinecraftClusterRef;

use crate::{condition::HasConditions, resourceref::ResourceRefSpec, resourceref::LocationResourceRefSpec, schemas::ImageOverrideSpec};

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
    #[serde(skip_serializing_if = "Option::is_none")]
    pub pod_overrides: Option<MinecraftServerPodOverridesSpec>,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerVersionSpec {
    /// Channel of the version to use. Defaults to Paper
    #[serde(default)]
    pub channel: MinecraftServerVersion,

    /// Name of the version to use
    pub name: String,

    /// Reference to a server JAR file to download and use instead of
    /// the built-in one
    #[serde(skip_serializing_if = "Option::is_none")]
    pub custom_jar: Option<ResourceRefSpec>,
}

#[derive(
    PartialEq, Deserialize, Serialize, Clone, Debug, Default, JsonSchema, IntoStaticStr, Display,
)]
pub enum MinecraftServerVersion {
    #[default]
    Paper,
    Folia,
    Minestom,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerConfigurationSpec {
    /// Name of an optional ConfigMap already containing the server
    /// configuration
    #[serde(skip_serializing_if = "Option::is_none")]
    pub existing_config_map_name: Option<String>,

    /// Reference to a world to download and extract. Gzipped tarball
    /// only
    #[serde(skip_serializing_if = "Option::is_none")]
    pub world: Option<ResourceRefSpec>,

    /// List of references to plugins to download
    #[serde(skip_serializing_if = "Option::is_none")]
    pub plugins: Option<Vec<ResourceRefSpec>>,

    /// List of optional references to patch archives to download
    /// and extract at the root of the server. Gzippied tarballs only
    #[serde(skip_serializing_if = "Option::is_none")]
    pub patches: Option<Vec<ResourceRefSpec>>,

    /// List of optional references to files to download
    /// and save at the defined path inside the server.
    #[serde(skip_serializing_if = "Option::is_none")]
    pub files: Option<Vec<LocationResourceRefSpec>>,

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
    #[serde(default, skip_serializing_if = "Option::is_none")]
    pub server_properties: Option<BTreeMap<String, String>>,

    /// Type of forwarding the proxies are using between themselves and
    /// this `MinecraftServer`
    #[serde(default)]
    pub proxy_forwarding_mode: MinecraftServerConfigurationProxyForwardingMode,

    /// Strategy to apply concerning Agones `GameServer` lifecycle management
    #[serde(default)]
    pub lifecycle_strategy: MinecraftServerConfigurationLifecycleStrategy,

    /// Whether to skip downloading the agent plugin on Pod startup. It way
    /// be useful if you are already building your own image with the agent
    /// plugin bundled
    #[schemars(default = "MinecraftServerConfigurationSpec::default_skip_agent_download")]
    pub skip_agent_download: bool,
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

    fn default_skip_agent_download() -> bool {
        false
    }
}

#[derive(
    PartialEq, Deserialize, Serialize, Clone, Debug, Default, JsonSchema, IntoStaticStr, Display,
)]
pub enum MinecraftServerConfigurationProxyForwardingMode {
    #[default]
    Velocity,
    BungeeCord,
}

#[derive(
    PartialEq, Deserialize, Serialize, Clone, Debug, Default, JsonSchema, IntoStaticStr, Display,
)]
pub enum MinecraftServerConfigurationLifecycleStrategy {
    #[default]
    AllocateWhenNotEmpty,
    Manual,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerPodOverridesSpec {
    /// Image to use as replacement for the built-in one
    #[serde(skip_serializing_if = "Option::is_none")]
    pub image: Option<ImageOverrideSpec>,

    /// Extra environment variables to add to the created `Pod`
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

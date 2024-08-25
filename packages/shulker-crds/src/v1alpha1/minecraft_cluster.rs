use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};
use strum::{Display, IntoStaticStr};

#[derive(CustomResource, Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[kube(
    kind = "MinecraftCluster",
    group = "shulkermc.io",
    version = "v1alpha1",
    namespaced,
    status = "MinecraftClusterStatus",
    printcolumn = r#"{"name": "Age", "type": "date", "jsonPath": ".metadata.creationTimestamp"}"#
)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftClusterSpec {
    /// List of player UUIDs that are automatically promoted as
    /// network administrators, which are granted all the permissions
    /// by default on all the proxies and servers
    #[serde(skip_serializing_if = "Option::is_none")]
    pub network_admins: Option<Vec<String>>,

    /// Redis configuration to use as a synchronization backend
    /// for the different Shulker components
    #[serde(skip_serializing_if = "Option::is_none")]
    pub redis: Option<MinecraftClusterRedisSpec>,

    /// List of servers that should be registered on the proxies
    /// that are not managed by Shulker
    #[serde(skip_serializing_if = "Option::is_none")]
    pub external_servers: Option<Vec<MinecraftClusterExternalServerSpec>>,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftClusterRedisSpec {
    /// Type of Redis deployment to use. Shulker can provided a single-node
    /// managed Redis to use for development purposes. Production workload
    /// should use a dedicated Redis cluster. Defaults to ManagedSingleNode
    pub type_: MinecraftClusterRedisDeploymentType,

    /// Configuration needed to connect to a provided Redis instance.
    /// If type is not `Provide`d, this field is ignored
    #[serde(default, skip_serializing_if = "Option::is_none")]
    pub provided: Option<MinecraftClusterRedisProvidedSpec>,
}

#[derive(
    PartialEq, Deserialize, Serialize, Clone, Debug, Default, JsonSchema, IntoStaticStr, Display,
)]
pub enum MinecraftClusterRedisDeploymentType {
    #[default]
    ManagedSingleNode,
    Provided,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftClusterRedisProvidedSpec {
    /// Host of the Redis instance
    pub host: String,

    /// Port of the Redis instance
    #[schemars(default = "MinecraftClusterRedisProvidedSpec::default_port")]
    pub port: u16,

    /// Kubernetes Secret containing the credentials to use. It must
    /// contains a `username` and `password` keys
    #[serde(default, skip_serializing_if = "Option::is_none")]
    pub credentials_secret_name: Option<String>,
}

#[cfg(not(tarpaulin_include))]
impl MinecraftClusterRedisProvidedSpec {
    fn default_port() -> u16 {
        6379
    }
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftClusterExternalServerSpec {
    /// Name of the server, as the proxies will register it.
    /// Allowed names only are lowercased, dash-separated
    /// alphanumerical string
    #[schemars(regex(pattern = r"^[a-z0-9\-]+$"))]
    pub name: String,

    /// Address of the server, may contain a port after a colon
    pub address: String,

    /// Tags associated to the server
    #[serde(default, skip_serializing_if = "Vec::is_empty")]
    pub tags: Vec<String>,
}

/// The status object of `MinecraftCluster`
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftClusterStatus {}

/// MinecraftClusterRef is to be used on resources referencing
/// a MinecraftCluster.
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftClusterRef {
    /// Name of the Kubernetes `MinecraftCluster` owning
    /// this resource
    pub name: String,
}

impl MinecraftClusterRef {
    /// Creates a new `MinecraftClusterRef` with the given name
    pub fn new(name: String) -> Self {
        Self { name }
    }
}

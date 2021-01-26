use kube::CustomResource;
use merge::Merge;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

use std::collections::HashMap;

use crate::resource::ResourceSpec;
use crate::template::{Template, TemplateSpec};
use shulker_common::merge::merge_hash_map;

/// Describe a MinecraftServerTemplate resource,
/// aka. a MinecraftServer schema.
/// 
/// A template could be partial, thus, a template could
/// depends on zero or more templates, recursively.
/// 
/// All the fields of the template should be mergeable.
#[derive(CustomResource, Deserialize, Serialize, Clone, Debug, JsonSchema, Merge)]
#[kube(
    group = "shulker.io",
    version = "v1beta1",
    kind = "MinecraftServerTemplate",
    namespaced
)]
#[kube(status = "MinecraftServerTemplateStatus")]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateSpec {
    /// List of templates to inherit properties from.
    pub inherit: Option<Vec<String>>,
    /// Is the template allowed to be used as root template
    /// of a MinecraftServer resource.
    pub schedulable: Option<bool>,
    /// Version of Minecraft to use.
    pub version: Option<MinecraftServerTemplateVersionSpec>,
    /// @todo Move the replication on the MinecraftServer resource itself
    pub replicas: Option<MinecraftServerTemplateReplicasSpec>,
    /// Assets to inject before starting the Minecraft server
    /// (maps, plugin or custom files).
    pub assets: Option<MinecraftServerTemplateAssetsSpec>,
    /// Number of players.
    pub players_count: Option<i32>,
    /// Additional properties to add to the server.properties
    /// file.
    #[merge(strategy = merge_hash_map)]
    pub additional_properties: Option<HashMap<String, String>>,
}

/// Describe the version of a Minecraft server.
/// A version is a pair of a name (1.16.5 for instance)
/// and a channel (vanilla for Mojang official server).
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateVersionSpec {
    /// Name of the version.
    pub name: String,
    /// Channel of the version.
    pub channel: String,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateReplicasSpec {
    pub min: Option<i32>,
    pub max: Option<i32>,
}

/// Describe the assets to inject on the Minecraft
/// server filesystem before starting it.
/// 
/// All the fields of this structure is a vector
/// of `ResourceSpec`s. The field only indicate the
/// extraction directory.
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Merge)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateAssetsSpec {
    /// Maps to inject (extracted from the server's root).
    pub maps: Option<Vec<ResourceSpec>>,
    /// Plugins to inject (extracted from the plugins
    /// subdirectory).
    pub plugins: Option<Vec<ResourceSpec>>,
}

/// Struct describing the status of a MinecraftServerTemplate
/// resource.
/// 
/// Will be automatically patched by the operator.
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateStatus {
    /// Result of the composition process of the
    /// template with its parents.
    pub compose_result: String,
}

/// Implementation of the `Template` trait for the
/// MinecraftServerTemplate resource.
impl Template<MinecraftServerTemplateSpec> for MinecraftServerTemplate {
    fn spec(&'_ self) -> &'_ MinecraftServerTemplateSpec {
        &self.spec
    }
}

/// Implementation of the `TemplateSpec` trait for the
/// MinecraftServerTemplate resource spec.
impl TemplateSpec for MinecraftServerTemplateSpec {
    fn inherit_from(&'_ self) -> &'_ Option<Vec<String>> {
        &self.inherit
    }
}

use kube::CustomResource;
use merge::Merge;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

use std::collections::HashMap;

use crate::resource::ResourceSpec;
use crate::template::{Template, TemplateSpec};
use shulker_common::merge::merge_hash_map;

#[derive(CustomResource, Clone, Debug, Deserialize, Serialize, Merge, JsonSchema)]
#[kube(
    group = "shulker.io",
    version = "v1beta1",
    kind = "MinecraftServerTemplate",
    namespaced
)]
#[kube(status = "MinecraftServerTemplateStatus")]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateSpec {
    pub inherit: Option<Vec<String>>,
    pub schedulable: Option<bool>,
    pub version: Option<MinecraftServerTemplateVersionSpec>,
    pub replicas: Option<MinecraftServerTemplateReplicasSpec>,
    pub assets: Option<MinecraftServerTemplateAssetsSpec>,
    pub players_count: Option<i32>,
    #[merge(strategy = merge_hash_map)]
    pub additional_properties: Option<HashMap<String, String>>,
}

#[derive(Clone, Debug, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateVersionSpec {
    pub name: String,
    pub channel: String,
}

#[derive(Clone, Debug, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateReplicasSpec {
    pub min: Option<i32>,
    pub max: Option<i32>,
}

#[derive(Clone, Debug, Serialize, Deserialize, Merge, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateAssetsSpec {
    pub maps: Option<Vec<ResourceSpec>>,
    pub plugins: Option<Vec<ResourceSpec>>,
}

#[derive(Clone, Debug, Serialize, Deserialize, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateStatus {
    pub instances: i32,
    pub players: i32,
    pub compose_result: String,
}

impl Template<MinecraftServerTemplateSpec> for MinecraftServerTemplate {
    fn spec(&'_ self) -> &'_ MinecraftServerTemplateSpec {
        &self.spec
    }
}

impl TemplateSpec for MinecraftServerTemplateSpec {
    fn inherit_from(&'_ self) -> &'_ Option<Vec<String>> {
        &self.inherit
    }
}

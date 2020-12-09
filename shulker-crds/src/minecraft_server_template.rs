use kube::CustomResource;
use merge::Merge;
use serde::{Deserialize, Serialize};

use std::collections::HashMap;

use crate::template::{Template, TemplateSpec};
use shulker_common::merge::merge_hash_map;

#[derive(CustomResource, Clone, Debug, Deserialize, Serialize, Merge)]
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
    pub version: Option<MinecraftServerTemplateVersionSpec>,
    pub count: Option<MinecraftServerTemplateCountSpec>,
    pub players_count: Option<i32>,
    #[merge(strategy = merge_hash_map)]
    pub additional_properties: Option<HashMap<String, String>>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateVersionSpec {
    pub name: String,
    pub channel: String,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateCountSpec {
    pub minimum: Option<i32>,
    pub maximum: Option<i32>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct MinecraftServerTemplateStatus {
    pub instances: i32,
    pub players: i32,
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

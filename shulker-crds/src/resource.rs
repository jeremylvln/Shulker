use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

use std::collections::HashMap;

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ResourceSpec {
    pub name: String,
    pub provider: String,
    pub spec: HashMap<String, String>,
}

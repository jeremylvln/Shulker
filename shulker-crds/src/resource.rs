use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

use std::collections::HashMap;

/// Describe a Resource, aka. an external
/// asset which should be retreived and cached
/// (if applicable).
///
/// A resource is a pair of a name (as a resource
/// could be shared in multiple places, a unique
/// name helps the operator tu avoid proxy duplication
/// for the same resource) and a provider (with its
/// spec).
///
/// The responsability of validating the provider's
/// spec is leaved to the provider itself.
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ResourceSpec {
    /// Name of the resource.
    pub name: String,
    /// Provider of the resource.
    pub provider: String,
    /// Spec applicable to the provider of the resource.
    pub spec: HashMap<String, String>,
}

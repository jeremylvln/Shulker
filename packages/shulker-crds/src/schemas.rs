use google_agones_crds::v1::fleet_autoscaler::FleetAutoscalerPolicySpec;
use kube::core::ObjectMeta;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema, Default)]
#[serde(rename_all = "camelCase")]
pub struct TemplateSpec<T> {
    /// Common metadata to add to the created objects
    pub metadata: Option<ObjectMeta>,

    /// The spec of the object to create from the template
    pub spec: T,
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct ImageOverrideSpec {
    /// Complete name of the image, including the repository name
    /// and tag
    pub name: String,

    /// Policy about when to pull the image
    #[schemars(default = "ImageOverrideSpec::default_pull_policy")]
    #[schemars(schema_with = "ImageOverrideSpec::schema_pull_policy")]
    pub pull_policy: String,

    ///  A list of secrets to use to pull the image
    pub image_pull_secrets: Vec<k8s_openapi::api::core::v1::LocalObjectReference>,
}

#[cfg(not(tarpaulin_include))]
impl ImageOverrideSpec {
    fn default_pull_policy() -> String {
        "IfNotPresent".to_string()
    }

    fn schema_pull_policy(_: &mut schemars::gen::SchemaGenerator) -> schemars::schema::Schema {
        schemars::schema::SchemaObject {
            instance_type: Some(schemars::schema::InstanceType::String.into()),
            enum_values: Some(vec![
                serde_json::Value::String("Always".to_string()),
                serde_json::Value::String("Never".to_string()),
                serde_json::Value::String("IfNotPresent".to_string()),
            ]),
            ..Default::default()
        }
        .into()
    }
}

#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct FleetAutoscalingSpec {
    pub agones_policy: Option<FleetAutoscalerPolicySpec>,
}

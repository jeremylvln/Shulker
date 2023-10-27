use k8s_openapi::api::core::v1::ConfigMap;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use crate::reconcilers::builder::ResourceBuilder;
use shulker_crds::v1alpha1::minecraft_server_fleet::MinecraftServerFleet;

use super::MinecraftServerFleetReconciler;

pub struct ConfigMapBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl<'a> ResourceBuilder<'a> for ConfigMapBuilder {
    type OwnerType = MinecraftServerFleet;
    type ResourceType = ConfigMap;
    type Context = ();

    fn name(minecraft_server_fleet: &Self::OwnerType) -> String {
        format!("{}-config", minecraft_server_fleet.name_any())
    }

    fn api(&self, minecraft_server_fleet: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(
            self.client.clone(),
            minecraft_server_fleet.namespace().as_ref().unwrap(),
        )
    }

    async fn build(
        &self,
        minecraft_server_fleet: &Self::OwnerType,
        name: &str,
        _existing_config_map: Option<&Self::ResourceType>,
        _context: Option<Self::Context>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let config_map = ConfigMap {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(minecraft_server_fleet.namespace().unwrap().clone()),
                labels: Some(
                    MinecraftServerFleetReconciler::get_labels(minecraft_server_fleet, "config".to_string(), "minecraft-server".to_string()),
                ),
                ..ObjectMeta::default()
            },
            data: Some(
                crate::reconcilers::minecraft_server::config_map::ConfigMapBuilder::get_data_from_spec(
                    &minecraft_server_fleet.spec.template.spec.config,
                ),
            ),
            ..ConfigMap::default()
        };

        Ok(config_map)
    }
}

impl ConfigMapBuilder {
    pub fn new(client: Client) -> Self {
        ConfigMapBuilder { client }
    }
}

#[cfg(test)]
mod tests {
    use crate::reconcilers::{
        builder::ResourceBuilder,
        minecraft_server_fleet::fixtures::{create_client_mock, TEST_SERVER_FLEET},
    };

    #[test]
    fn name_contains_fleet_name() {
        // W
        let name = super::ConfigMapBuilder::name(&TEST_SERVER_FLEET);

        // T
        assert_eq!(name, "my-server-config");
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::ConfigMapBuilder::new(client);
        let name = super::ConfigMapBuilder::name(&TEST_SERVER_FLEET);

        // W
        let config_map = builder
            .build(&TEST_SERVER_FLEET, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(config_map);
    }
}

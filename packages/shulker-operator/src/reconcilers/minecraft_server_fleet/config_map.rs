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
impl ResourceBuilder for ConfigMapBuilder {
    type OwnerType = MinecraftServerFleet;
    type ResourceType = ConfigMap;

    fn name(minecraft_server_fleet: &Self::OwnerType) -> String {
        format!("{}-config", minecraft_server_fleet.name_any())
    }

    fn is_updatable() -> bool {
        true
    }

    fn api(&self, minecraft_server_fleet: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(
            self.client.clone(),
            minecraft_server_fleet.namespace().as_ref().unwrap(),
        )
    }

    fn is_needed(&self, _minecraft_server_fleet: &Self::OwnerType) -> bool {
        true
    }

    async fn create(
        &self,
        minecraft_server_fleet: &Self::OwnerType,
        name: &str,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let config_map = ConfigMap {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(minecraft_server_fleet.namespace().unwrap().clone()),
                labels: Some(
                    MinecraftServerFleetReconciler::get_common_labels(minecraft_server_fleet)
                        .into_iter()
                        .collect(),
                ),
                ..ObjectMeta::default()
            },
            ..ConfigMap::default()
        };

        Ok(config_map)
    }

    async fn update(
        &self,
        minecraft_server_fleet: &Self::OwnerType,
        config_map: &mut Self::ResourceType,
    ) -> Result<(), anyhow::Error> {
        config_map.data = Some(
            crate::reconcilers::minecraft_server::config_map::ConfigMapBuilder::get_data_from_spec(
                &minecraft_server_fleet.spec.template.spec.config,
            ),
        );
        Ok(())
    }
}

impl ConfigMapBuilder {
    pub fn new(client: Client) -> Self {
        ConfigMapBuilder { client }
    }
}

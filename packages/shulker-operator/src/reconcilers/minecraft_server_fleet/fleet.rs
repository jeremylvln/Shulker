use std::collections::BTreeMap;

use google_agones_crds::v1::fleet::FleetTemplate;
use k8s_openapi::api::apps::v1::DeploymentStrategy;
use k8s_openapi::apimachinery::pkg::apis::meta::v1::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use crate::reconcilers::builder::ResourceBuilder;
use crate::resources::resourceref_resolver::ResourceRefResolver;
use google_agones_crds::v1::fleet::Fleet;
use google_agones_crds::v1::fleet::FleetSpec;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServer;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServerSpec;
use shulker_crds::v1alpha1::minecraft_server_fleet::MinecraftServerFleet;

use super::config_map::ConfigMapBuilder;
use super::MinecraftServerFleetReconciler;

pub struct FleetBuilder {
    client: Client,
    resourceref_resolver: ResourceRefResolver,
}

#[async_trait::async_trait]
impl<'a> ResourceBuilder<'a> for FleetBuilder {
    type OwnerType = MinecraftServerFleet;
    type ResourceType = Fleet;
    type Context = ();

    fn name(minecraft_server_fleet: &Self::OwnerType) -> String {
        minecraft_server_fleet.name_any()
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
        _existing_fleet: Option<&Self::ResourceType>,
        _context: Option<Self::Context>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let mut config_clone = minecraft_server_fleet.spec.template.spec.config.clone();
        config_clone.existing_config_map_name = Some(ConfigMapBuilder::name(minecraft_server_fleet));

        let fake_mincraft_server = MinecraftServer {
            metadata: ObjectMeta {
                namespace: minecraft_server_fleet.namespace(),
                name: Some(minecraft_server_fleet.name_any()),
                ..ObjectMeta::default()
            },
            spec: MinecraftServerSpec {
                cluster_ref: minecraft_server_fleet
                    .spec
                    .template
                    .spec
                    .cluster_ref
                    .clone(),
                config: config_clone,
                ..minecraft_server_fleet.spec.template.spec.clone()
            },
            status: None,
        };

        let game_server_spec = crate::reconcilers::minecraft_server::gameserver::GameServerBuilder::get_game_server_spec(
            &self.resourceref_resolver,
            &fake_mincraft_server,
        ).await?;
        let replicas = match &minecraft_server_fleet.spec.autoscaling {
            Some(_) => 0,
            None => minecraft_server_fleet.spec.replicas as i32,
        };

        let mut template_labels = MinecraftServerFleetReconciler::get_labels(
            minecraft_server_fleet,
            "minecraft-server".to_string(),
            "minecraft-server".to_string(),
        );
        template_labels.extend(
            game_server_spec
                .template
                .metadata
                .as_ref()
                .map_or_else(BTreeMap::new, |metadata| {
                    metadata.labels.clone().unwrap_or_default()
                }),
        );

        let mut template_annotations = BTreeMap::<String, String>::new();
        template_annotations.extend(
            game_server_spec
                .template
                .metadata
                .as_ref()
                .map_or_else(BTreeMap::new, |metadata| {
                    metadata.annotations.clone().unwrap_or_default()
                }),
        );

        if let Some(metadata) = &minecraft_server_fleet.spec.template.metadata {
            if let Some(additional_labels) = metadata.labels.clone() {
                template_labels.extend(additional_labels);
            }

            if let Some(additional_annotations) = metadata.annotations.clone() {
                template_annotations.extend(additional_annotations);
            }
        }

        let fleet = Fleet {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(minecraft_server_fleet.namespace().unwrap().clone()),
                labels: Some(MinecraftServerFleetReconciler::get_labels(
                    minecraft_server_fleet,
                    "minecraft-server".to_string(),
                    "minecraft-server".to_string(),
                )),
                ..ObjectMeta::default()
            },
            spec: FleetSpec {
                replicas: Some(replicas),
                strategy: Some(DeploymentStrategy {
                    type_: Some("Recreate".to_string()),
                    ..DeploymentStrategy::default()
                }),
                scheduling: Some("Packed".to_string()),
                template: FleetTemplate {
                    metadata: Some(ObjectMeta {
                        labels: Some(template_labels),
                        annotations: Some(template_annotations),
                        ..ObjectMeta::default()
                    }),
                    spec: game_server_spec,
                },
            },
            status: None,
        };

        Ok(fleet)
    }
}

impl FleetBuilder {
    pub fn new(client: Client) -> Self {
        FleetBuilder {
            client: client.clone(),
            resourceref_resolver: ResourceRefResolver::new(client.clone()),
        }
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
        let name = super::FleetBuilder::name(&TEST_SERVER_FLEET);

        // T
        assert_eq!(name, "my-server");
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let name = super::FleetBuilder::name(&TEST_SERVER_FLEET);

        // W
        let fleet = builder
            .build(&TEST_SERVER_FLEET, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(fleet);
    }

    #[tokio::test]
    async fn build_should_merge_labels() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let name = super::FleetBuilder::name(&TEST_SERVER_FLEET);

        // W
        let fleet = builder
            .build(&TEST_SERVER_FLEET, &name, None, None)
            .await
            .unwrap();

        // T
        let additional_labels = TEST_SERVER_FLEET
            .spec
            .template
            .metadata
            .as_ref()
            .unwrap()
            .labels
            .as_ref()
            .unwrap();
        additional_labels.iter().for_each(|(key, value)| {
            assert_eq!(
                fleet
                    .spec
                    .template
                    .metadata
                    .as_ref()
                    .unwrap()
                    .labels
                    .as_ref()
                    .unwrap()
                    .get(key)
                    .unwrap(),
                value
            );
        });
    }

    #[tokio::test]
    async fn build_should_merge_annotations() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let name = super::FleetBuilder::name(&TEST_SERVER_FLEET);

        // W
        let fleet = builder
            .build(&TEST_SERVER_FLEET, &name, None, None)
            .await
            .unwrap();

        // T
        let additional_annotations = TEST_SERVER_FLEET
            .spec
            .template
            .metadata
            .as_ref()
            .unwrap()
            .annotations
            .as_ref()
            .unwrap();
        additional_annotations.iter().for_each(|(key, value)| {
            assert_eq!(
                fleet
                    .spec
                    .template
                    .metadata
                    .as_ref()
                    .unwrap()
                    .annotations
                    .as_ref()
                    .unwrap()
                    .get(key)
                    .unwrap(),
                value
            );
        });
    }

    #[tokio::test]
    async fn build_should_use_recreate_strategy() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let name = super::FleetBuilder::name(&TEST_SERVER_FLEET);

        // W
        let fleet = builder
            .build(&TEST_SERVER_FLEET, &name, None, None)
            .await
            .unwrap();

        // T
        assert_eq!(
            fleet
                .spec
                .strategy
                .as_ref()
                .unwrap()
                .type_
                .as_ref()
                .unwrap(),
            "Recreate"
        );
    }
}

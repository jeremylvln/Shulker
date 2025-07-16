use std::collections::BTreeMap;

use google_agones_crds::v1::fleet::FleetTemplate;
use k8s_openapi::api::apps::v1::DeploymentStrategy;
use k8s_openapi::apimachinery::pkg::apis::meta::v1::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;

use crate::agent::AgentConfig;
use crate::reconcilers::minecraft_server::gameserver::GameServerBuilderContext;
use crate::resources::resourceref_resolver::ResourceRefResolver;
use google_agones_crds::v1::fleet::Fleet;
use google_agones_crds::v1::fleet::FleetSpec;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServer;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServerSpec;
use shulker_crds::v1alpha1::minecraft_server_fleet::MinecraftServerFleet;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

use super::config_map::ConfigMapBuilder;
use super::MinecraftServerFleetReconciler;

pub struct FleetBuilder {
    client: Client,
    resourceref_resolver: ResourceRefResolver,
}

#[derive(Clone, Debug)]
pub struct FleetBuilderContext<'a> {
    pub cluster: &'a MinecraftCluster,
    pub agent_config: &'a AgentConfig,
}

#[async_trait::async_trait]
impl<'a> ResourceBuilder<'a> for FleetBuilder {
    type OwnerType = MinecraftServerFleet;
    type ResourceType = Fleet;
    type Context = FleetBuilderContext<'a>;

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
        context: Option<FleetBuilderContext<'a>>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let mut config_clone = minecraft_server_fleet.spec.template.spec.config.clone();
        config_clone.existing_config_map_name =
            Some(ConfigMapBuilder::name(minecraft_server_fleet));

        let mut template_labels = MinecraftServerFleetReconciler::get_labels(
            minecraft_server_fleet,
            "minecraft-server".to_string(),
            "minecraft-server".to_string(),
        );
        let mut template_annotations = BTreeMap::<String, String>::from([(
            "minecraftserver.shulkermc.io/tags".to_string(),
            minecraft_server_fleet.spec.template.spec.tags.join(","),
        )]);

        if let Some(metadata) = &minecraft_server_fleet.spec.template.metadata {
            if let Some(additional_labels) = metadata.labels.clone() {
                template_labels.extend(additional_labels);
            }

            if let Some(additional_annotations) = metadata.annotations.clone() {
                template_annotations.extend(additional_annotations);
            }
        }

        let fake_mincraft_server = MinecraftServer {
            metadata: ObjectMeta {
                namespace: minecraft_server_fleet.namespace(),
                name: Some(minecraft_server_fleet.name_any()),
                labels: Some(template_labels.clone()),
                annotations: Some(template_annotations.clone()),
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

        let game_server_context = GameServerBuilderContext {
            cluster: context.as_ref().unwrap().cluster,
            agent_config: context.as_ref().unwrap().agent_config,
            owning_fleet: Some(minecraft_server_fleet),
        };

        let game_server_spec = crate::reconcilers::minecraft_server::gameserver::GameServerBuilder::get_game_server_spec(
            &self.resourceref_resolver,
            &game_server_context,
            &fake_mincraft_server,
        ).await?;
        let replicas = match &minecraft_server_fleet.spec.autoscaling {
            Some(_) => 0,
            None => minecraft_server_fleet.spec.replicas as i32,
        };

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
    use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

    use crate::{
        agent::AgentConfig,
        constants,
        reconcilers::{
            minecraft_cluster::fixtures::TEST_CLUSTER,
            minecraft_server_fleet::fixtures::{create_client_mock, TEST_SERVER_FLEET},
        },
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
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let fleet = builder
            .build(&TEST_SERVER_FLEET, &name, None, Some(context))
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
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let fleet = builder
            .build(&TEST_SERVER_FLEET, &name, None, Some(context))
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
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let fleet = builder
            .build(&TEST_SERVER_FLEET, &name, None, Some(context))
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
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let fleet = builder
            .build(&TEST_SERVER_FLEET, &name, None, Some(context))
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

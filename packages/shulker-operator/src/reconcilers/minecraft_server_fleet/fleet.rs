use std::collections::BTreeMap;

use k8s_openapi::api::apps::v1::DeploymentStrategy;
use k8s_openapi::apimachinery::pkg::apis::meta::v1::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use crate::reconcilers::builder::ResourceBuilder;
use crate::resources::resourceref_resolver::ResourceRefResolver;
use shulker_crds::agones::fleet::Fleet;
use shulker_crds::agones::fleet::FleetSpec;
use shulker_crds::agones::game_server::GameServerSpec;
use shulker_crds::schemas::TemplateSpec;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServer;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServerConfigurationSpec;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServerSpec;
use shulker_crds::v1alpha1::minecraft_server_fleet::MinecraftServerFleet;

use super::config_map::ConfigMapBuilder;
use super::MinecraftServerFleetReconciler;

pub struct FleetBuilder {
    client: Client,
    resourceref_resolver: ResourceRefResolver,
}

#[async_trait::async_trait]
impl ResourceBuilder for FleetBuilder {
    type OwnerType = MinecraftServerFleet;
    type ResourceType = Fleet;

    fn name(minecraft_server_fleet: &Self::OwnerType) -> String {
        minecraft_server_fleet.name_any()
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
        let fleet = Fleet {
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
            spec: FleetSpec::default(),
            status: None,
        };

        Ok(fleet)
    }

    async fn update(
        &self,
        minecraft_server_fleet: &Self::OwnerType,
        fleet: &mut Self::ResourceType,
    ) -> Result<(), anyhow::Error> {
        let fake_mincraft_server = MinecraftServer {
            metadata: ObjectMeta {
                namespace: minecraft_server_fleet.namespace(),
                ..ObjectMeta::default()
            },
            spec: MinecraftServerSpec {
                cluster_ref: minecraft_server_fleet
                    .spec
                    .template
                    .spec
                    .cluster_ref
                    .clone(),
                config: MinecraftServerConfigurationSpec {
                    existing_config_map_name: Some(ConfigMapBuilder::name(&minecraft_server_fleet)),
                    ..MinecraftServerConfigurationSpec::default()
                },
                ..MinecraftServerSpec::default()
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

        let mut labels = MinecraftServerFleetReconciler::get_common_labels(minecraft_server_fleet);
        labels.extend(
            game_server_spec
                .template
                .metadata
                .as_ref()
                .map_or_else(BTreeMap::new, |metadata| {
                    metadata.labels.clone().unwrap_or_default()
                }),
        );

        let mut annotations = BTreeMap::<String, String>::new();
        annotations.extend(
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
                labels.extend(additional_labels);
            }

            if let Some(additional_annotations) = metadata.annotations.clone() {
                annotations.extend(additional_annotations);
            }
        }

        fleet.spec = FleetSpec {
            replicas: Some(replicas),
            strategy: Some(DeploymentStrategy {
                type_: Some("Recreate".to_string()),
                ..DeploymentStrategy::default()
            }),
            scheduling: Some("Packed".to_string()),
            template: TemplateSpec::<GameServerSpec> {
                metadata: Some(ObjectMeta {
                    labels: Some(labels),
                    annotations: Some(annotations),
                    ..ObjectMeta::default()
                }),
                spec: game_server_spec,
            },
        };

        Ok(())
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

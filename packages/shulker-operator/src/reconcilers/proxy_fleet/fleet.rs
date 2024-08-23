use std::collections::BTreeMap;

use google_agones_crds::v1::fleet::FleetTemplate;
use k8s_openapi::api::apps::v1::DeploymentStrategy;
use k8s_openapi::api::apps::v1::RollingUpdateDeployment;
use k8s_openapi::api::core::v1::Capabilities;
use k8s_openapi::api::core::v1::ConfigMapVolumeSource;
use k8s_openapi::api::core::v1::Container;
use k8s_openapi::api::core::v1::ContainerPort;
use k8s_openapi::api::core::v1::EmptyDirVolumeSource;
use k8s_openapi::api::core::v1::EnvVar;
use k8s_openapi::api::core::v1::EnvVarSource;
use k8s_openapi::api::core::v1::ExecAction;
use k8s_openapi::api::core::v1::ObjectFieldSelector;
use k8s_openapi::api::core::v1::PodSpec;
use k8s_openapi::api::core::v1::PodTemplateSpec;
use k8s_openapi::api::core::v1::Probe;
use k8s_openapi::api::core::v1::SecretKeySelector;
use k8s_openapi::api::core::v1::SecretVolumeSource;
use k8s_openapi::api::core::v1::SecurityContext;
use k8s_openapi::api::core::v1::Volume;
use k8s_openapi::api::core::v1::VolumeMount;
use k8s_openapi::apimachinery::pkg::apis::meta::v1::ObjectMeta;
use k8s_openapi::apimachinery::pkg::util::intstr::IntOrString;
use kube::Api;
use kube::Client;
use kube::ResourceExt;
use lazy_static::lazy_static;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;
use shulker_crds::v1alpha1::proxy_fleet::ProxyFleetTemplateVersion;
use url::Url;

use crate::agent::AgentConfig;
use crate::constants;
use crate::reconcilers::agent::get_agent_plugin_url;
use crate::reconcilers::agent::AgentSide;
use crate::reconcilers::minecraft_cluster::external_servers_config_map::ExternalServersConfigMapBuilder;
use crate::reconcilers::redis_ref::RedisRef;
use crate::resources::resourceref_resolver::ResourceRefResolver;
use google_agones_crds::v1::fleet::Fleet;
use google_agones_crds::v1::fleet::FleetSpec;
use google_agones_crds::v1::game_server::GameServerEvictionSpec;
use google_agones_crds::v1::game_server::GameServerHealthSpec;
use google_agones_crds::v1::game_server::GameServerSpec;
use shulker_crds::v1alpha1::proxy_fleet::ProxyFleet;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

use super::config_map::ConfigMapBuilder;
use super::ProxyFleetReconciler;

const PROXY_SHULKER_CONFIG_DIR: &str = "/mnt/shulker/config";
const PROXY_SHULKER_FORWARDING_SECRET_DIR: &str = "/mnt/shulker/forwarding-secret";
const PROXY_DATA_DIR: &str = "/server";
const PROXY_DRAIN_LOCK_DIR: &str = "/mnt/drain-lock";
const PROXY_SHULKER_EXTERNAL_SERVERS_DIR: &str = "/mnt/shulker/external-servers";

lazy_static! {
    static ref PROXY_SECURITY_CONTEXT: SecurityContext = SecurityContext {
        allow_privilege_escalation: Some(false),
        read_only_root_filesystem: Some(true),
        run_as_non_root: Some(true),
        run_as_user: Some(1000),
        capabilities: Some(Capabilities {
            drop: Some(vec!["ALL".to_string()]),
            ..Capabilities::default()
        }),
        ..SecurityContext::default()
    };
}

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
    type OwnerType = ProxyFleet;
    type ResourceType = Fleet;
    type Context = FleetBuilderContext<'a>;

    fn name(proxy_fleet: &Self::OwnerType) -> String {
        proxy_fleet.name_any()
    }

    fn api(&self, proxy_fleet: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(
            self.client.clone(),
            proxy_fleet.namespace().as_ref().unwrap(),
        )
    }

    async fn build(
        &self,
        proxy_fleet: &Self::OwnerType,
        name: &str,
        _existing_fleet: Option<&Self::ResourceType>,
        context: Option<FleetBuilderContext<'a>>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let game_server_spec = self
            .get_game_server_spec(context.as_ref().unwrap(), proxy_fleet)
            .await?;
        let replicas = match &proxy_fleet.spec.autoscaling {
            Some(_) => 0,
            None => proxy_fleet.spec.replicas as i32,
        };

        let fleet = Fleet {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(proxy_fleet.namespace().unwrap().clone()),
                labels: Some(ProxyFleetReconciler::get_labels(
                    proxy_fleet,
                    "proxy".to_string(),
                    "proxy".to_string(),
                )),
                ..ObjectMeta::default()
            },
            spec: FleetSpec {
                replicas: Some(replicas),
                strategy: Some(DeploymentStrategy {
                    type_: Some("RollingUpdate".to_string()),
                    rolling_update: Some(RollingUpdateDeployment {
                        max_unavailable: Some(IntOrString::String("25%".to_string())),
                        max_surge: Some(IntOrString::String("25%".to_string())),
                    }),
                }),
                scheduling: Some("Packed".to_string()),
                template: FleetTemplate {
                    metadata: game_server_spec.template.metadata.clone(),
                    spec: game_server_spec,
                },
            },
            status: None,
        };

        Ok(fleet)
    }
}

impl<'a> FleetBuilder {
    pub fn new(client: Client) -> Self {
        FleetBuilder {
            client: client.clone(),
            resourceref_resolver: ResourceRefResolver::new(client.clone()),
        }
    }

    async fn get_game_server_spec(
        &self,
        context: &FleetBuilderContext<'a>,
        proxy_fleet: &ProxyFleet,
    ) -> Result<GameServerSpec, anyhow::Error> {
        let pod_template_spec = self.get_pod_template_spec(context, proxy_fleet).await?;
        let game_server_spec = GameServerSpec {
            ports: Some(vec![]),
            eviction: Some(GameServerEvictionSpec {
                safe: "OnUpgrade".to_string(),
            }),
            health: Some(GameServerHealthSpec {
                disabled: Some(false),
                initial_delay_seconds: Some(30),
                period_seconds: Some(10),
                failure_threshold: Some(5),
            }),
            template: pod_template_spec,
        };

        Ok(game_server_spec)
    }

    async fn get_pod_template_spec(
        &self,
        context: &FleetBuilderContext<'a>,
        proxy_fleet: &ProxyFleet,
    ) -> Result<PodTemplateSpec, anyhow::Error> {
        let mut pod_spec = PodSpec {
            init_containers: Some(vec![Container {
                image: Some("alpine:latest".to_string()),
                name: "init-fs".to_string(),
                command: Some(vec![
                    "sh".to_string(),
                    format!("{}/init-fs.sh", PROXY_SHULKER_CONFIG_DIR),
                ]),
                env: Some(self.get_init_env(context, proxy_fleet).await?),
                security_context: Some(PROXY_SECURITY_CONTEXT.clone()),
                volume_mounts: Some(vec![
                    VolumeMount {
                        name: "shulker-config".to_string(),
                        mount_path: PROXY_SHULKER_CONFIG_DIR.to_string(),
                        read_only: Some(true),
                        ..VolumeMount::default()
                    },
                    VolumeMount {
                        name: "proxy-data".to_string(),
                        mount_path: PROXY_DATA_DIR.to_string(),
                        ..VolumeMount::default()
                    },
                ]),
                ..Container::default()
            }]),
            containers: vec![Container {
                image: Some(constants::PROXY_IMAGE.to_string()),
                name: "proxy".to_string(),
                ports: Some(vec![ContainerPort {
                    name: Some("minecraft".to_string()),
                    container_port: 25577,
                    ..ContainerPort::default()
                }]),
                env: Some(self.get_env(context, proxy_fleet)?),
                readiness_probe: Some(Probe {
                    exec: Some(ExecAction {
                        command: Some(vec![
                            "bash".to_string(),
                            format!("{}/probe-readiness.sh", PROXY_DATA_DIR),
                        ]),
                    }),
                    initial_delay_seconds: Some(10),
                    period_seconds: Some(10),
                    ..Probe::default()
                }),
                image_pull_policy: Some("IfNotPresent".to_string()),
                security_context: Some(PROXY_SECURITY_CONTEXT.clone()),
                volume_mounts: Some(self.get_volume_mounts(context, proxy_fleet)),
                ..Container::default()
            }],
            service_account_name: Some(format!(
                "shulker-{}-proxy",
                &proxy_fleet.spec.cluster_ref.name
            )),
            restart_policy: Some("Never".to_string()),
            volumes: Some(self.get_volumes(context, proxy_fleet)),
            ..PodSpec::default()
        };

        if let Some(pod_overrides) = &proxy_fleet.spec.template.spec.pod_overrides {
            if let Some(image_overrides) = &pod_overrides.image {
                if let Some(name) = image_overrides.name.as_ref() {
                    pod_spec.containers[0].image = Some(name.clone())
                }

                if let Some(pull_policy) = image_overrides.pull_policy.as_ref() {
                    pod_spec.containers[0].image_pull_policy = Some(pull_policy.clone())
                }

                if let Some(image_pull_secrets) = image_overrides.image_pull_secrets.as_ref() {
                    pod_spec.image_pull_secrets = Some(image_pull_secrets.clone())
                }
            }

            if let Some(resources_overrides) = &pod_overrides.resources {
                pod_spec.containers[0].resources = Some(resources_overrides.clone());
            }

            if let Some(affinity_overrides) = &pod_overrides.affinity {
                pod_spec.affinity = Some(affinity_overrides.clone());
            }

            if let Some(node_selector_overrides) = &pod_overrides.node_selector {
                pod_spec.node_selector =
                    Some(node_selector_overrides.clone().into_iter().collect());
            }

            pod_spec.tolerations = pod_overrides.tolerations.clone();

            if let Some(volume_mounts_overrides) = &pod_overrides.volume_mounts {
                pod_spec.containers[0]
                    .volume_mounts
                    .as_mut()
                    .unwrap()
                    .append(&mut volume_mounts_overrides.clone());
            }

            if let Some(volumes_override) = &pod_overrides.volumes {
                pod_spec
                    .volumes
                    .as_mut()
                    .unwrap()
                    .append(&mut volumes_override.clone());
            }

            if let Some(ports_overrides) = &pod_overrides.ports {
                pod_spec.containers[0]
                    .ports
                    .as_mut()
                    .unwrap()
                    .append(&mut ports_overrides.clone());
            }
        }

        let mut pod_labels =
            ProxyFleetReconciler::get_labels(proxy_fleet, "proxy".to_string(), "proxy".to_string());
        let mut pod_annotations = BTreeMap::<String, String>::from([(
            "kubectl.kubernetes.io/default-container".to_string(),
            "proxy".to_string(),
        )]);

        if let Some(metadata) = &proxy_fleet.spec.template.metadata {
            if let Some(additional_labels) = metadata.labels.clone() {
                pod_labels.extend(additional_labels);
            }

            if let Some(additional_annotations) = metadata.annotations.clone() {
                pod_annotations.extend(additional_annotations);
            }
        }

        Ok(PodTemplateSpec {
            metadata: Some(ObjectMeta {
                labels: Some(pod_labels),
                annotations: Some(pod_annotations),
                ..ObjectMeta::default()
            }),
            spec: Some(pod_spec),
        })
    }

    async fn get_init_env(
        &self,
        context: &FleetBuilderContext<'a>,
        proxy_fleet: &ProxyFleet,
    ) -> Result<Vec<EnvVar>, anyhow::Error> {
        let spec = &proxy_fleet.spec.template.spec;

        let plugin_urls =
            FleetBuilder::get_plugin_urls(&self.resourceref_resolver, context, proxy_fleet).await?;

        let mut env: Vec<EnvVar> = vec![
            EnvVar {
                name: "SHULKER_CONFIG_DIR".to_string(),
                value: Some(PROXY_SHULKER_CONFIG_DIR.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_PROXY_DATA_DIR".to_string(),
                value: Some(PROXY_DATA_DIR.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_VERSION_CHANNEL".to_string(),
                value: Some(spec.version.channel.to_string()),
                ..EnvVar::default()
            },
        ];

        if !plugin_urls.is_empty() {
            let urls: Vec<String> = plugin_urls.into_iter().map(|url| url.to_string()).collect();

            env.push(EnvVar {
                name: "SHULKER_PROXY_PLUGIN_URLS".to_string(),
                value: Some(urls.join(";")),
                ..EnvVar::default()
            })
        }

        if let Some(patches) = &spec.config.patches {
            let urls: Vec<String> = self
                .resourceref_resolver
                .resolve_all(proxy_fleet.namespace().as_ref().unwrap(), patches)
                .await?
                .into_iter()
                .map(|url| url.to_string())
                .collect();

            env.push(EnvVar {
                name: "SHULKER_PROXY_PATCH_URLS".to_string(),
                value: Some(urls.join(";")),
                ..EnvVar::default()
            })
        }

        Ok(env)
    }

    fn get_env(
        &self,
        context: &FleetBuilderContext<'a>,
        proxy_fleet: &ProxyFleet,
    ) -> Result<Vec<EnvVar>, anyhow::Error> {
        let spec = &proxy_fleet.spec.template.spec;
        let redis_ref = RedisRef::from_cluster(context.cluster)?;

        let mut env: Vec<EnvVar> = vec![
            EnvVar {
                name: "SHULKER_CLUSTER_NAME".to_string(),
                value: Some(context.cluster.name_any()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_PROXY_NAMESPACE".to_string(),
                value_from: Some(EnvVarSource {
                    field_ref: Some(ObjectFieldSelector {
                        field_path: "metadata.namespace".to_string(),
                        ..ObjectFieldSelector::default()
                    }),
                    ..EnvVarSource::default()
                }),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_PROXY_NAME".to_string(),
                value_from: Some(EnvVarSource {
                    field_ref: Some(ObjectFieldSelector {
                        field_path: "metadata.name".to_string(),
                        ..ObjectFieldSelector::default()
                    }),
                    ..EnvVarSource::default()
                }),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_PROXY_FLEET_NAME".to_string(),
                value: Some(proxy_fleet.name_any()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_PROXY_TTL_SECONDS".to_string(),
                value: Some(spec.config.ttl_seconds.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_PROXY_PLAYER_DELTA_BEFORE_EXCLUSION".to_string(),
                value: Some(spec.config.players_delta_before_exclusion.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_PROXY_REDIS_HOST".to_string(),
                value: Some(redis_ref.host),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_PROXY_REDIS_PORT".to_string(),
                value: Some(redis_ref.port.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "TYPE".to_string(),
                value: Some(Self::get_type_from_version_channel(&spec.version.channel)),
                ..EnvVar::default()
            },
            EnvVar {
                name: Self::get_version_env_from_version_channel(&spec.version.channel),
                value: Some(spec.version.name.clone()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "HEALTH_USE_PROXY".to_string(),
                value: Some(spec.config.proxy_protocol.to_string()),
                ..EnvVar::default()
            },
        ];

        if let Some(network_admins) = context.cluster.spec.network_admins.as_ref() {
            env.push(EnvVar {
                name: "SHULKER_NETWORK_ADMINS".to_string(),
                value: Some(network_admins.join(",")),
                ..EnvVar::default()
            })
        }

        if let Some(preferred_reconnection_address) = proxy_fleet
            .spec
            .service
            .as_ref()
            .and_then(|x| x.preferred_reconnection_address.as_ref())
        {
            env.push(EnvVar {
                name: "SHULKER_PROXY_PREFERRED_RECONNECT_ADDRESS".to_string(),
                value: Some(preferred_reconnection_address.clone()),
                ..EnvVar::default()
            })
        }

        if let Some(redis_ref_credentials_secret_name) = redis_ref.credentials_secret_name.as_ref()
        {
            env.append(&mut vec![
                EnvVar {
                    name: "SHULKER_PROXY_REDIS_USERNAME".to_string(),
                    value_from: Some(EnvVarSource {
                        secret_key_ref: Some(SecretKeySelector {
                            name: Some(redis_ref_credentials_secret_name.clone()),
                            key: "username".to_string(),
                            ..SecretKeySelector::default()
                        }),
                        ..EnvVarSource::default()
                    }),
                    ..EnvVar::default()
                },
                EnvVar {
                    name: "SHULKER_PROXY_REDIS_PASSWORD".to_string(),
                    value_from: Some(EnvVarSource {
                        secret_key_ref: Some(SecretKeySelector {
                            name: Some(redis_ref_credentials_secret_name.clone()),
                            key: "password".to_string(),
                            ..SecretKeySelector::default()
                        }),
                        ..EnvVarSource::default()
                    }),
                    ..EnvVar::default()
                },
            ])
        }

        if let Some(pod_overrides) = &spec.pod_overrides {
            if let Some(env_overrides) = &pod_overrides.env {
                env.extend(env_overrides.clone());
            }
        }

        Ok(env)
    }

    async fn get_plugin_urls(
        resourceref_resolver: &ResourceRefResolver,
        context: &FleetBuilderContext<'a>,
        proxy_fleet: &ProxyFleet,
    ) -> Result<Vec<Url>, anyhow::Error> {
        let agent_platform = match proxy_fleet.spec.template.spec.version.channel {
            ProxyFleetTemplateVersion::Velocity => Some("velocity".to_string()),
            ProxyFleetTemplateVersion::BungeeCord | ProxyFleetTemplateVersion::Waterfall => {
                Some("bungeecord".to_string())
            }
        };

        let mut plugin_refs: Vec<Url> = vec![];

        if !proxy_fleet.spec.template.spec.config.skip_agent_download {
            if let Some(agent_platform) = agent_platform {
                plugin_refs.push(
                    get_agent_plugin_url(
                        resourceref_resolver,
                        context.agent_config,
                        AgentSide::Proxy,
                        agent_platform,
                    )
                    .await?,
                )
            }
        }

        if let Some(plugins) = &proxy_fleet.spec.template.spec.config.plugins {
            plugin_refs.extend(
                resourceref_resolver
                    .resolve_all(proxy_fleet.namespace().as_ref().unwrap(), plugins)
                    .await?,
            );
        }

        Ok(plugin_refs)
    }

    fn get_type_from_version_channel(channel: &ProxyFleetTemplateVersion) -> String {
        match channel {
            ProxyFleetTemplateVersion::Velocity => "VELOCITY".to_string(),
            ProxyFleetTemplateVersion::BungeeCord => "BUNGEECORD".to_string(),
            ProxyFleetTemplateVersion::Waterfall => "WATERFALL".to_string(),
        }
    }

    fn get_version_env_from_version_channel(channel: &ProxyFleetTemplateVersion) -> String {
        match channel {
            ProxyFleetTemplateVersion::Velocity => "VELOCITY_BUILD_ID".to_string(),
            ProxyFleetTemplateVersion::BungeeCord => "BUNGEE_JOB_ID".to_string(),
            ProxyFleetTemplateVersion::Waterfall => "WATERFALL_BUILD_ID".to_string(),
        }
    }

    fn get_volumes(
        &self,
        context: &FleetBuilderContext<'a>,
        proxy_fleet: &ProxyFleet,
    ) -> Vec<Volume> {
        let mut volumes = vec![
            Volume {
                name: "shulker-config".to_string(),
                config_map: Some(ConfigMapVolumeSource {
                    name: Some(
                        proxy_fleet
                            .spec
                            .template
                            .spec
                            .config
                            .existing_config_map_name
                            .clone()
                            .unwrap_or_else(|| ConfigMapBuilder::name(proxy_fleet)),
                    ),
                    ..ConfigMapVolumeSource::default()
                }),
                ..Volume::default()
            },
            Volume {
                name: "shulker-forwarding-secret".to_string(),
                secret: Some(SecretVolumeSource {
                    secret_name: Some(format!(
                        "{}-forwarding-secret",
                        &proxy_fleet.spec.cluster_ref.name
                    )),
                    ..SecretVolumeSource::default()
                }),
                ..Volume::default()
            },
            Volume {
                name: "proxy-data".to_string(),
                empty_dir: Some(EmptyDirVolumeSource::default()),
                ..Volume::default()
            },
            Volume {
                name: "proxy-drain-lock".to_string(),
                empty_dir: Some(EmptyDirVolumeSource::default()),
                ..Volume::default()
            },
            Volume {
                name: "proxy-tmp".to_string(),
                empty_dir: Some(EmptyDirVolumeSource::default()),
                ..Volume::default()
            },
        ];

        let has_external_servers = context
            .cluster
            .spec
            .external_servers
            .as_ref()
            .map_or(false, |list| !list.is_empty());

        if has_external_servers {
            volumes.push(Volume {
                name: "shulker-external-servers".to_string(),
                config_map: Some(ConfigMapVolumeSource {
                    name: Some(ExternalServersConfigMapBuilder::name(context.cluster)),
                    ..ConfigMapVolumeSource::default()
                }),
                ..Volume::default()
            })
        }

        volumes
    }

    fn get_volume_mounts(
        &self,
        context: &FleetBuilderContext<'a>,
        _proxy_fleet: &ProxyFleet,
    ) -> Vec<VolumeMount> {
        let mut volume_mounts = vec![
            VolumeMount {
                name: "shulker-forwarding-secret".to_string(),
                mount_path: PROXY_SHULKER_FORWARDING_SECRET_DIR.to_string(),
                read_only: Some(true),
                ..VolumeMount::default()
            },
            VolumeMount {
                name: "proxy-data".to_string(),
                mount_path: PROXY_DATA_DIR.to_string(),
                ..VolumeMount::default()
            },
            VolumeMount {
                name: "proxy-drain-lock".to_string(),
                mount_path: PROXY_DRAIN_LOCK_DIR.to_string(),
                read_only: Some(true),
                ..VolumeMount::default()
            },
            VolumeMount {
                name: "proxy-tmp".to_string(),
                mount_path: "/tmp".to_string(),
                ..VolumeMount::default()
            },
        ];

        let has_external_servers = context
            .cluster
            .spec
            .external_servers
            .as_ref()
            .map_or(false, |list| !list.is_empty());

        if has_external_servers {
            volume_mounts.push(VolumeMount {
                name: "shulker-external-servers".to_string(),
                mount_path: PROXY_SHULKER_EXTERNAL_SERVERS_DIR.to_string(),
                read_only: Some(true),
                ..VolumeMount::default()
            })
        }

        volume_mounts
    }
}

#[cfg(test)]
mod tests {
    use k8s_openapi::api::core::v1::{
        ContainerPort, EmptyDirVolumeSource, LocalObjectReference, Volume, VolumeMount,
    };
    use shulker_crds::schemas::ImageOverrideSpec;
    use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

    use crate::{
        agent::AgentConfig,
        constants,
        reconcilers::{
            minecraft_cluster::fixtures::TEST_CLUSTER,
            proxy_fleet::fixtures::{create_client_mock, TEST_PROXY_FLEET},
        },
        resources::resourceref_resolver::ResourceRefResolver,
    };

    #[test]
    fn name_contains_fleet_name() {
        // W
        let name = super::FleetBuilder::name(&TEST_PROXY_FLEET);

        // T
        assert_eq!(name, "my-proxy");
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let name = super::FleetBuilder::name(&TEST_PROXY_FLEET);
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let fleet = builder
            .build(&TEST_PROXY_FLEET, &name, None, Some(context))
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
        let name = super::FleetBuilder::name(&TEST_PROXY_FLEET);
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let fleet = builder
            .build(&TEST_PROXY_FLEET, &name, None, Some(context))
            .await
            .unwrap();

        // T
        let additional_labels = TEST_PROXY_FLEET
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
        let name = super::FleetBuilder::name(&TEST_PROXY_FLEET);
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let fleet = builder
            .build(&TEST_PROXY_FLEET, &name, None, Some(context))
            .await
            .unwrap();

        // T
        let additional_annotations = TEST_PROXY_FLEET
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
    async fn get_pod_template_spec_use_config_override() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let mut proxy_fleet = TEST_PROXY_FLEET.clone();
        proxy_fleet
            .spec
            .template
            .spec
            .config
            .existing_config_map_name = Some("my_way_better_config".to_string());
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = builder
            .get_pod_template_spec(&context, &proxy_fleet)
            .await
            .unwrap();

        // T
        let shulker_config_volume = pod_template
            .spec
            .as_ref()
            .unwrap()
            .volumes
            .as_ref()
            .unwrap()
            .iter()
            .find(|volume| volume.name == "shulker-config");
        assert!(shulker_config_volume.is_some());
        assert!(
            shulker_config_volume
                .unwrap()
                .config_map
                .as_ref()
                .unwrap()
                .name
                .as_ref()
                .unwrap()
                == "my_way_better_config"
        )
    }

    #[tokio::test]
    async fn get_pod_template_spec_contains_image_override() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let mut proxy_fleet = TEST_PROXY_FLEET.clone();
        proxy_fleet
            .spec
            .template
            .spec
            .pod_overrides
            .as_mut()
            .unwrap()
            .image = Some(ImageOverrideSpec {
            name: Some("my_better_image".to_string()),
            ..ImageOverrideSpec::default()
        });
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = builder
            .get_pod_template_spec(&context, &proxy_fleet)
            .await
            .unwrap();

        // T
        assert_eq!(
            pod_template.spec.as_ref().unwrap().containers[0].image,
            Some("my_better_image".to_string())
        );
    }

    #[tokio::test]
    async fn get_pod_template_spec_contains_image_pull_secrets() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let mut proxy_fleet = TEST_PROXY_FLEET.clone();
        proxy_fleet
            .spec
            .template
            .spec
            .pod_overrides
            .as_mut()
            .unwrap()
            .image = Some(ImageOverrideSpec {
            image_pull_secrets: Some(vec![LocalObjectReference {
                name: Some("my_pull_secret".to_string()),
            }]),
            ..ImageOverrideSpec::default()
        });
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = builder
            .get_pod_template_spec(&context, &proxy_fleet)
            .await
            .unwrap();

        // T
        assert_eq!(
            pod_template.spec.as_ref().unwrap().image_pull_secrets,
            Some(vec![LocalObjectReference {
                name: Some("my_pull_secret".to_string())
            }])
        );
    }

    #[tokio::test]
    async fn get_pod_template_spec_contains_volume_mounts() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let mut proxy_fleet = TEST_PROXY_FLEET.clone();
        proxy_fleet
            .spec
            .template
            .spec
            .pod_overrides
            .as_mut()
            .unwrap()
            .volume_mounts = Some(vec![VolumeMount {
            name: "my_extra_volume".to_string(),
            mount_path: "/mnt/path".to_string(),
            ..VolumeMount::default()
        }]);
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = builder
            .get_pod_template_spec(&context, &proxy_fleet)
            .await
            .unwrap();

        // T
        let extra_volume_mount = pod_template.spec.as_ref().unwrap().containers[0]
            .volume_mounts
            .as_ref()
            .unwrap()
            .iter()
            .find(|volume_mount| volume_mount.name == "my_extra_volume");
        assert!(extra_volume_mount.is_some());
    }

    #[tokio::test]
    async fn get_pod_template_spec_contains_volumes() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let mut proxy_fleet = TEST_PROXY_FLEET.clone();
        proxy_fleet
            .spec
            .template
            .spec
            .pod_overrides
            .as_mut()
            .unwrap()
            .volumes = Some(vec![Volume {
            name: "my_extra_volume".to_string(),
            empty_dir: Some(EmptyDirVolumeSource::default()),
            ..Volume::default()
        }]);
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = builder
            .get_pod_template_spec(&context, &proxy_fleet)
            .await
            .unwrap();

        // T
        let extra_volume = pod_template
            .spec
            .as_ref()
            .unwrap()
            .volumes
            .as_ref()
            .unwrap()
            .iter()
            .find(|volume| volume.name == "my_extra_volume");
        assert!(extra_volume.is_some());
    }

    #[tokio::test]
    async fn get_pod_template_spec_contains_ports() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let mut proxy_fleet = TEST_PROXY_FLEET.clone();
        proxy_fleet
            .spec
            .template
            .spec
            .pod_overrides
            .as_mut()
            .unwrap()
            .ports = Some(vec![ContainerPort {
            name: Some("metrics".to_string()),
            container_port: 9090,
            ..ContainerPort::default()
        }]);
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = builder
            .get_pod_template_spec(&context, &proxy_fleet)
            .await
            .unwrap();

        // T
        let extra_port = pod_template.spec.as_ref().unwrap().containers[0]
            .ports
            .as_ref()
            .unwrap()
            .iter()
            .find(|port| port.name == Some("metrics".to_owned()));
        assert!(extra_port.is_some());
    }

    #[tokio::test]
    async fn get_init_env_contains_plugins() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let env = builder
            .get_init_env(&context, &TEST_PROXY_FLEET)
            .await
            .unwrap();

        // T
        let plugins_env = env
            .iter()
            .find(|env| env.name == "SHULKER_PROXY_PLUGIN_URLS")
            .unwrap();
        assert_eq!(
            plugins_env.value.as_ref().unwrap(),
            "https://maven.jeremylvln.fr/artifactory/shulker-snapshots/io/shulkermc/shulker-proxy-agent/0.0.0-test-cfg/shulker-proxy-agent-0.0.0-test-cfg-velocity.jar;https://example.com/my_plugin.jar"
        );
    }

    #[tokio::test]
    async fn get_init_env_contains_patches() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let env = builder
            .get_init_env(&context, &TEST_PROXY_FLEET)
            .await
            .unwrap();

        // T
        let patches_env = env
            .iter()
            .find(|env| env.name == "SHULKER_PROXY_PATCH_URLS")
            .unwrap();
        assert_eq!(
            patches_env.value.as_ref().unwrap(),
            "https://example.com/my_patch.tar.gz"
        );
    }

    #[tokio::test]
    async fn get_env_contains_preferred_reconnect_address() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let mut proxy_fleet = TEST_PROXY_FLEET.clone();
        proxy_fleet
            .spec
            .service
            .as_mut()
            .unwrap()
            .preferred_reconnection_address = Some("127.0.0.1".to_string());
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let env = builder.get_env(&context, &proxy_fleet).unwrap();

        // T
        let address_env = env
            .iter()
            .find(|env| env.name == "SHULKER_PROXY_PREFERRED_RECONNECT_ADDRESS")
            .unwrap();
        assert_eq!(address_env.value.as_ref().unwrap(), "127.0.0.1");
    }

    #[tokio::test]
    async fn get_env_merges_env_overrides() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let env = builder.get_env(&context, &TEST_PROXY_FLEET).unwrap();

        // T
        TEST_PROXY_FLEET
            .spec
            .template
            .spec
            .pod_overrides
            .as_ref()
            .unwrap()
            .env
            .as_ref()
            .unwrap()
            .iter()
            .for_each(|env_override| {
                assert!(env.contains(env_override));
            });
    }

    #[tokio::test]
    async fn get_plugin_urls_skip_agent() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let mut proxy_fleet = TEST_PROXY_FLEET.clone();
        proxy_fleet.spec.template.spec.config.skip_agent_download = true;
        proxy_fleet.spec.template.spec.config.plugins = None;
        let context = super::FleetBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let plugin_urls =
            super::FleetBuilder::get_plugin_urls(&resourceref_resolver, &context, &proxy_fleet)
                .await
                .unwrap();

        // T
        assert!(plugin_urls.is_empty())
    }
}

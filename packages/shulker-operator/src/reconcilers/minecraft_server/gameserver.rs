use std::collections::BTreeMap;

use k8s_openapi::api::core::v1::Capabilities;
use k8s_openapi::api::core::v1::ConfigMapVolumeSource;
use k8s_openapi::api::core::v1::Container;
use k8s_openapi::api::core::v1::ContainerPort;
use k8s_openapi::api::core::v1::EmptyDirVolumeSource;
use k8s_openapi::api::core::v1::EnvVar;
use k8s_openapi::api::core::v1::EnvVarSource;
use k8s_openapi::api::core::v1::ObjectFieldSelector;
use k8s_openapi::api::core::v1::PodSpec;
use k8s_openapi::api::core::v1::PodTemplateSpec;
use k8s_openapi::api::core::v1::SecretKeySelector;
use k8s_openapi::api::core::v1::SecurityContext;
use k8s_openapi::api::core::v1::Volume;
use k8s_openapi::api::core::v1::VolumeMount;
use k8s_openapi::apimachinery::pkg::apis::meta::v1::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;
use lazy_static::lazy_static;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServerVersion;
use shulker_kube_utils::reconcilers::BuilderReconcilerError;
use url::Url;

use crate::agent::AgentConfig;
use crate::constants;
use crate::reconcilers::agent::get_agent_plugin_url;
use crate::reconcilers::agent::AgentSide;
use crate::resources::resourceref_resolver::ResourceRefResolver;
use google_agones_crds::v1::game_server::GameServer;
use google_agones_crds::v1::game_server::GameServerEvictionSpec;
use google_agones_crds::v1::game_server::GameServerHealthSpec;
use google_agones_crds::v1::game_server::GameServerSpec;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServer;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

use super::config_map::ConfigMapBuilder;
use super::MinecraftServerReconciler;

const MINECRAFT_SERVER_SHULKER_CONFIG_DIR: &str = "/mnt/shulker/config";
const MINECRAFT_SERVER_CONFIG_DIR: &str = "/config";
const MINECRAFT_SERVER_DATA_DIR: &str = "/data";

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

pub struct GameServerBuilder {
    client: Client,
    resourceref_resolver: ResourceRefResolver,
}

#[derive(Clone, Debug)]
pub struct GameServerBuilderContext<'a> {
    pub cluster: &'a MinecraftCluster,
    pub agent_config: &'a AgentConfig,
}

#[async_trait::async_trait]
impl<'a> ResourceBuilder<'a> for GameServerBuilder {
    type OwnerType = MinecraftServer;
    type ResourceType = GameServer;
    type Context = GameServerBuilderContext<'a>;

    fn name(minecraft_server: &Self::OwnerType) -> String {
        minecraft_server.name_any()
    }

    fn api(&self, minecraft_server: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(
            self.client.clone(),
            minecraft_server.namespace().as_ref().unwrap(),
        )
    }

    async fn build(
        &self,
        minecraft_server: &Self::OwnerType,
        name: &str,
        _existing_game_server: Option<&Self::ResourceType>,
        context: Option<GameServerBuilderContext<'a>>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        GameServerBuilder::validate_spec(context.as_ref().unwrap(), minecraft_server)?;

        let game_server = GameServer {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(minecraft_server.namespace().unwrap().clone()),
                labels: Some(MinecraftServerReconciler::get_labels(
                    minecraft_server,
                    "minecraft-server".to_string(),
                    "minecraft-server".to_string(),
                )),
                annotations: Some(BTreeMap::<String, String>::from([(
                    "minecraftserver.shulkermc.io/tags".to_string(),
                    minecraft_server.spec.tags.join(","),
                )])),
                ..ObjectMeta::default()
            },
            spec: Self::get_game_server_spec(
                &self.resourceref_resolver,
                context.as_ref().unwrap(),
                minecraft_server,
            )
            .await?,
            status: None,
        };

        Ok(game_server)
    }
}

impl<'a> GameServerBuilder {
    pub fn new(client: Client) -> Self {
        GameServerBuilder {
            client: client.clone(),
            resourceref_resolver: ResourceRefResolver::new(client.clone()),
        }
    }

    pub fn validate_spec(
        _context: &GameServerBuilderContext<'a>,
        minecraft_server: &MinecraftServer,
    ) -> Result<(), BuilderReconcilerError> {
        if minecraft_server.spec.version.channel == MinecraftServerVersion::Minestom
            && minecraft_server.spec.version.custom_jar.is_none()
        {
            return Err(BuilderReconcilerError::ValidationError(
                std::any::type_name::<GameServerBuilder>(),
                "a Minestom-based server requires a custom JAR to be provided".to_string(),
            ));
        }

        Ok(())
    }

    pub async fn get_game_server_spec(
        resourceref_resolver: &ResourceRefResolver,
        context: &GameServerBuilderContext<'a>,
        minecraft_server: &MinecraftServer,
    ) -> Result<GameServerSpec, anyhow::Error> {
        let pod_template_spec =
            Self::get_pod_template_spec(resourceref_resolver, context, minecraft_server).await?;

        let game_server_spec = GameServerSpec {
            ports: Some(vec![]),
            eviction: Some(GameServerEvictionSpec {
                safe: "OnUpgrade".to_string(),
            }),
            health: Some(GameServerHealthSpec {
                disabled: Some(false),
                initial_delay_seconds: Some(30),
                period_seconds: Some(15),
                failure_threshold: Some(5),
            }),
            template: pod_template_spec,
        };

        Ok(game_server_spec)
    }

    async fn get_pod_template_spec(
        resourceref_resolver: &ResourceRefResolver,
        context: &GameServerBuilderContext<'a>,
        minecraft_server: &MinecraftServer,
    ) -> Result<PodTemplateSpec, anyhow::Error> {
        let mut pod_spec = PodSpec {
            init_containers: Some(vec![Container {
                image: Some("alpine:latest".to_string()),
                name: "init-fs".to_string(),
                command: Some(vec![
                    "sh".to_string(),
                    format!("{}/init-fs.sh", MINECRAFT_SERVER_SHULKER_CONFIG_DIR),
                ]),
                env: Some(
                    Self::get_init_env(resourceref_resolver, context, minecraft_server).await?,
                ),
                security_context: Some(PROXY_SECURITY_CONTEXT.clone()),
                volume_mounts: Some(vec![
                    VolumeMount {
                        name: "shulker-config".to_string(),
                        mount_path: MINECRAFT_SERVER_SHULKER_CONFIG_DIR.to_string(),
                        read_only: Some(true),
                        ..VolumeMount::default()
                    },
                    VolumeMount {
                        name: "server-config".to_string(),
                        mount_path: MINECRAFT_SERVER_CONFIG_DIR.to_string(),
                        ..VolumeMount::default()
                    },
                ]),
                ..Container::default()
            }]),
            containers: vec![Container {
                image: Some(constants::MINECRAFT_SERVER_IMAGE.to_string()),
                name: "minecraft-server".to_string(),
                ports: Some(vec![ContainerPort {
                    name: Some("minecraft".to_string()),
                    container_port: 25565,
                    ..ContainerPort::default()
                }]),
                env: Some(Self::get_env(resourceref_resolver, context, minecraft_server).await?),
                image_pull_policy: Some("IfNotPresent".to_string()),
                security_context: Some(PROXY_SECURITY_CONTEXT.clone()),
                volume_mounts: Some(vec![
                    VolumeMount {
                        name: "server-config".to_string(),
                        mount_path: MINECRAFT_SERVER_CONFIG_DIR.to_string(),
                        ..VolumeMount::default()
                    },
                    VolumeMount {
                        name: "server-data".to_string(),
                        mount_path: MINECRAFT_SERVER_DATA_DIR.to_string(),
                        ..VolumeMount::default()
                    },
                    VolumeMount {
                        name: "server-tmp".to_string(),
                        mount_path: "/tmp".to_string(),
                        ..VolumeMount::default()
                    },
                ]),
                ..Container::default()
            }],
            subdomain: Some(format!(
                "{}-cluster",
                &minecraft_server.spec.cluster_ref.name
            )),
            service_account_name: Some(format!(
                "shulker-{}-server",
                &minecraft_server.spec.cluster_ref.name
            )),
            restart_policy: Some("Never".to_string()),
            volumes: Some(vec![
                Volume {
                    name: "shulker-config".to_string(),
                    config_map: Some(ConfigMapVolumeSource {
                        name: minecraft_server
                            .spec
                            .config
                            .existing_config_map_name
                            .clone()
                            .unwrap_or_else(|| ConfigMapBuilder::name(minecraft_server)),
                        ..ConfigMapVolumeSource::default()
                    }),
                    ..Volume::default()
                },
                Volume {
                    name: "server-config".to_string(),
                    empty_dir: Some(EmptyDirVolumeSource::default()),
                    ..Volume::default()
                },
                Volume {
                    name: "server-data".to_string(),
                    empty_dir: Some(EmptyDirVolumeSource::default()),
                    ..Volume::default()
                },
                Volume {
                    name: "server-tmp".to_string(),
                    empty_dir: Some(EmptyDirVolumeSource::default()),
                    ..Volume::default()
                },
            ]),
            ..PodSpec::default()
        };

        if let Some(pod_overrides) = &minecraft_server.spec.pod_overrides {
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

        let mut pod_labels = minecraft_server.labels().clone();
        pod_labels.append(&mut MinecraftServerReconciler::get_labels(
            minecraft_server,
            "minecraft-server".to_string(),
            "minecraft-server".to_string(),
        ));

        let mut pod_annotations = minecraft_server.annotations().clone();
        pod_annotations.append(&mut BTreeMap::<String, String>::from([(
            "kubectl.kubernetes.io/default-container".to_string(),
            "minecraft-server".to_string(),
        )]));

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
        resourceref_resolver: &ResourceRefResolver,
        context: &GameServerBuilderContext<'a>,
        minecraft_server: &MinecraftServer,
    ) -> Result<Vec<EnvVar>, anyhow::Error> {
        let spec = &minecraft_server.spec;

        let plugin_urls =
            GameServerBuilder::get_plugin_urls(resourceref_resolver, context, minecraft_server)
                .await?;

        let mut env: Vec<EnvVar> = vec![
            EnvVar {
                name: "SHULKER_CONFIG_DIR".to_string(),
                value: Some(MINECRAFT_SERVER_SHULKER_CONFIG_DIR.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_SERVER_CONFIG_DIR".to_string(),
                value: Some(MINECRAFT_SERVER_CONFIG_DIR.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_SERVER_DATA_DIR".to_string(),
                value: Some(MINECRAFT_SERVER_DATA_DIR.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_VERSION_CHANNEL".to_string(),
                value: Some(spec.version.channel.to_string()),
                ..EnvVar::default()
            },
        ];

        if let Some(world) = &spec.config.world {
            let url = resourceref_resolver
                .resolve(minecraft_server.namespace().as_ref().unwrap(), world)
                .await?
                .as_url()?;

            env.push(EnvVar {
                name: "SHULKER_SERVER_WORLD_URL".to_string(),
                value: Some(url.to_string()),
                ..EnvVar::default()
            })
        }

        if !plugin_urls.is_empty() {
            let urls: Vec<String> = plugin_urls.into_iter().map(|url| url.to_string()).collect();

            env.push(EnvVar {
                name: "SHULKER_SERVER_PLUGIN_URLS".to_string(),
                value: Some(urls.join(";")),
                ..EnvVar::default()
            })
        }

        if let Some(patches) = &spec.config.patches {
            let urls: Vec<String> = resourceref_resolver
                .resolve_all(minecraft_server.namespace().as_ref().unwrap(), patches)
                .await?
                .into_iter()
                .map(|url| url.to_string())
                .collect();

            env.push(EnvVar {
                name: "SHULKER_SERVER_PATCH_URLS".to_string(),
                value: Some(urls.join(";")),
                ..EnvVar::default()
            })
        }

        Ok(env)
    }

    async fn get_env(
        resourceref_resolver: &ResourceRefResolver,
        context: &GameServerBuilderContext<'a>,
        minecraft_server: &MinecraftServer,
    ) -> Result<Vec<EnvVar>, anyhow::Error> {
        let spec = &minecraft_server.spec;

        let mut env: Vec<EnvVar> = vec![
            EnvVar {
                name: "SHULKER_SERVER_NAME".to_string(),
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
                name: "SHULKER_SERVER_NAMESPACE".to_string(),
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
                name: "SHULKER_NETWORK_ADMINS".to_string(),
                value: Some(
                    context
                        .cluster
                        .spec
                        .network_admins
                        .as_ref()
                        .map(|list| list.join(","))
                        .unwrap_or("".to_string()),
                ),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_SERVER_LIFECYCLE_STRATEGY".to_string(),
                value: Some(minecraft_server.spec.config.lifecycle_strategy.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "EULA".to_string(),
                value: Some("TRUE".to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "COPY_CONFIG_DEST".to_string(),
                value: Some(MINECRAFT_SERVER_DATA_DIR.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SYNC_SKIP_NEWER_IN_DESTINATION".to_string(),
                value: Some("true".to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SKIP_SERVER_PROPERTIES".to_string(),
                value: Some("true".to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "REPLACE_ENV_IN_PLACE".to_string(),
                value: Some("true".to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "REPLACE_ENV_VARIABLE_PREFIX".to_string(),
                value: Some("CFG_".to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "CFG_VELOCITY_FORWARDING_SECRET".to_string(),
                value_from: Some(EnvVarSource {
                    secret_key_ref: Some(SecretKeySelector {
                        name: format!("{}-forwarding-secret", spec.cluster_ref.name),
                        key: "key".to_string(),
                        ..SecretKeySelector::default()
                    }),
                    ..EnvVarSource::default()
                }),
                ..EnvVar::default()
            },
            EnvVar {
                name: "MEMORY".to_string(),
                value: Some("".to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "JVM_XX_OPTS".to_string(),
                value: Some("-XX:MaxRAMPercentage=75".to_string()),
                ..EnvVar::default()
            },
        ];

        if let Some(custom_jar) = spec.version.custom_jar.as_ref() {
            env.append(&mut vec![
                EnvVar {
                    name: "TYPE".to_string(),
                    value: Some("CUSTOM".to_string()),
                    ..EnvVar::default()
                },
                EnvVar {
                    name: "CUSTOM_SERVER".to_string(),
                    value: Some(
                        resourceref_resolver
                            .resolve(minecraft_server.namespace().as_ref().unwrap(), custom_jar)
                            .await?
                            .as_url()?
                            .to_string(),
                    ),
                    ..EnvVar::default()
                },
            ]);
        } else {
            env.append(&mut vec![
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
            ]);
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
        context: &GameServerBuilderContext<'a>,
        minecraft_server: &MinecraftServer,
    ) -> Result<Vec<Url>, anyhow::Error> {
        let agent_platform = match minecraft_server.spec.version.channel {
            MinecraftServerVersion::Paper | MinecraftServerVersion::Folia => {
                Some("paper".to_string())
            }
            MinecraftServerVersion::Minestom => None,
        };

        let mut plugin_refs: Vec<Url> = vec![];

        if !minecraft_server.spec.config.skip_agent_download {
            if let Some(agent_platform) = agent_platform {
                plugin_refs.push(
                    get_agent_plugin_url(
                        resourceref_resolver,
                        context.agent_config,
                        AgentSide::Server,
                        agent_platform,
                    )
                    .await?,
                )
            }
        }

        if let Some(plugins) = &minecraft_server.spec.config.plugins {
            plugin_refs.extend(
                resourceref_resolver
                    .resolve_all(minecraft_server.namespace().as_ref().unwrap(), plugins)
                    .await?,
            );
        }

        Ok(plugin_refs)
    }

    fn get_type_from_version_channel(channel: &MinecraftServerVersion) -> String {
        match channel {
            MinecraftServerVersion::Paper | MinecraftServerVersion::Minestom => "PAPER".to_string(),
            MinecraftServerVersion::Folia => "FOLIA".to_string(),
        }
    }

    fn get_version_env_from_version_channel(_channel: &MinecraftServerVersion) -> String {
        "VERSION".to_string()
    }
}

#[cfg(test)]
mod tests {
    use k8s_openapi::api::core::v1::{
        ContainerPort, EmptyDirVolumeSource, LocalObjectReference, Volume, VolumeMount,
    };
    use shulker_crds::{
        resourceref::ResourceRefSpec, schemas::ImageOverrideSpec,
        v1alpha1::minecraft_server::MinecraftServerVersion,
    };
    use shulker_kube_utils::reconcilers::{builder::ResourceBuilder, BuilderReconcilerError};

    use crate::{
        agent::AgentConfig,
        constants,
        reconcilers::{
            minecraft_cluster::fixtures::TEST_CLUSTER,
            minecraft_server::fixtures::{create_client_mock, TEST_SERVER},
        },
        resources::resourceref_resolver::ResourceRefResolver,
    };

    use super::GameServerBuilder;

    #[test]
    fn name_contains_server_name() {
        // W
        let name = super::GameServerBuilder::name(&TEST_SERVER);

        // T
        assert_eq!(name, "my-server");
    }

    #[test]
    fn validate_rejects_minestom_without_custom_jar() {
        // G
        let mut server = TEST_SERVER.clone();
        server.spec.version.channel = MinecraftServerVersion::Minestom;
        server.spec.version.custom_jar = None;
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let is_valid = GameServerBuilder::validate_spec(&context, &server);

        // T
        match is_valid.unwrap_err() {
            BuilderReconcilerError::ValidationError(_, message) => {
                assert_eq!(
                    message,
                    "a Minestom-based server requires a custom JAR to be provided"
                );
            }
            _ => {
                unreachable!("Error mismatch")
            }
        }
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::GameServerBuilder::new(client);
        let name = super::GameServerBuilder::name(&TEST_SERVER);
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let game_server = builder
            .build(&TEST_SERVER, &name, None, Some(context))
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(game_server);
    }

    #[tokio::test]
    async fn get_pod_template_spec_use_config_override() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let mut server = TEST_SERVER.clone();
        server.spec.config.existing_config_map_name = Some("my_way_better_config".to_string());
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = super::GameServerBuilder::get_pod_template_spec(
            &resourceref_resolver,
            &context,
            &server,
        )
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
            .find(|volume: &&Volume| volume.name == "shulker-config");
        assert!(shulker_config_volume.is_some());
        assert!(
            shulker_config_volume
                .unwrap()
                .config_map
                .as_ref()
                .unwrap()
                .name
                == "my_way_better_config"
        )
    }

    #[tokio::test]
    async fn get_pod_template_spec_contains_image_override() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let mut server = TEST_SERVER.clone();
        server.spec.pod_overrides.as_mut().unwrap().image = Some(ImageOverrideSpec {
            name: Some("my_better_image".to_string()),
            ..ImageOverrideSpec::default()
        });
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = super::GameServerBuilder::get_pod_template_spec(
            &resourceref_resolver,
            &context,
            &server,
        )
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
        let resourceref_resolver = ResourceRefResolver::new(client);
        let mut server = TEST_SERVER.clone();
        server.spec.pod_overrides.as_mut().unwrap().image = Some(ImageOverrideSpec {
            image_pull_secrets: Some(vec![LocalObjectReference {
                name: "my_pull_secret".to_string(),
            }]),
            ..ImageOverrideSpec::default()
        });
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = super::GameServerBuilder::get_pod_template_spec(
            &resourceref_resolver,
            &context,
            &server,
        )
        .await
        .unwrap();

        // T
        assert_eq!(
            pod_template.spec.as_ref().unwrap().image_pull_secrets,
            Some(vec![LocalObjectReference {
                name: "my_pull_secret".to_string()
            }])
        );
    }

    #[tokio::test]
    async fn get_pod_template_spec_contains_volume_mounts() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let mut server = TEST_SERVER.clone();
        server.spec.pod_overrides.as_mut().unwrap().volume_mounts = Some(vec![VolumeMount {
            name: "my_extra_volume".to_string(),
            mount_path: "/mnt/path".to_string(),
            ..VolumeMount::default()
        }]);
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = super::GameServerBuilder::get_pod_template_spec(
            &resourceref_resolver,
            &context,
            &server,
        )
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
        let resourceref_resolver = ResourceRefResolver::new(client);
        let mut server = TEST_SERVER.clone();
        server.spec.pod_overrides.as_mut().unwrap().volumes = Some(vec![Volume {
            name: "my_extra_volume".to_string(),
            empty_dir: Some(EmptyDirVolumeSource::default()),
            ..Volume::default()
        }]);
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = super::GameServerBuilder::get_pod_template_spec(
            &resourceref_resolver,
            &context,
            &server,
        )
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
        let resourceref_resolver = ResourceRefResolver::new(client);
        let mut server = TEST_SERVER.clone();
        server.spec.pod_overrides.as_mut().unwrap().ports = Some(vec![ContainerPort {
            name: Some("metrics".to_owned()),
            container_port: 9090,
            ..ContainerPort::default()
        }]);
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let pod_template = super::GameServerBuilder::get_pod_template_spec(
            &resourceref_resolver,
            &context,
            &server,
        )
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
    async fn get_init_env_contains_world() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let env =
            super::GameServerBuilder::get_init_env(&resourceref_resolver, &context, &TEST_SERVER)
                .await
                .unwrap();

        // T
        let world_env = env
            .iter()
            .find(|env| env.name == "SHULKER_SERVER_WORLD_URL")
            .unwrap();
        assert_eq!(
            world_env.value.as_ref().unwrap(),
            "https://example.com/my_world.tar.gz"
        );
    }

    #[tokio::test]
    async fn get_init_env_contains_plugins() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let env =
            super::GameServerBuilder::get_init_env(&resourceref_resolver, &context, &TEST_SERVER)
                .await
                .unwrap();

        // T
        let plugins_env = env
            .iter()
            .find(|env| env.name == "SHULKER_SERVER_PLUGIN_URLS")
            .unwrap();
        assert_eq!(
            plugins_env.value.as_ref().unwrap(),
            "https://maven.jeremylvln.fr/repository/shulker-snapshots/io/shulkermc/shulker-server-agent/0.0.0-test-cfg/shulker-server-agent-0.0.0-test-cfg-paper.jar;https://example.com/my_plugin.jar"
        );
    }

    #[tokio::test]
    async fn get_init_env_contains_patches() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let env =
            super::GameServerBuilder::get_init_env(&resourceref_resolver, &context, &TEST_SERVER)
                .await
                .unwrap();

        // T
        let patches_env = env
            .iter()
            .find(|env| env.name == "SHULKER_SERVER_PATCH_URLS")
            .unwrap();
        assert_eq!(
            patches_env.value.as_ref().unwrap(),
            "https://example.com/my_patch.tar.gz"
        );
    }

    #[tokio::test]
    async fn get_env_with_custom_jar() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let mut server = TEST_SERVER.clone();
        server.spec.version.custom_jar = Some(ResourceRefSpec {
            url: Some("https://example.com/my_custom.jar".to_string()),
            ..ResourceRefSpec::default()
        });
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let env = super::GameServerBuilder::get_env(&resourceref_resolver, &context, &server)
            .await
            .unwrap();

        // T
        let type_env = env.iter().find(|env| env.name == "TYPE").unwrap();
        assert_eq!(type_env.value.as_ref().unwrap(), "CUSTOM");
        let custom_server = env.iter().find(|env| env.name == "CUSTOM_SERVER").unwrap();
        assert_eq!(
            custom_server.value.as_ref().unwrap(),
            "https://example.com/my_custom.jar"
        );
    }

    #[tokio::test]
    async fn get_env_merges_env_overrides() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let env = super::GameServerBuilder::get_env(&resourceref_resolver, &context, &TEST_SERVER)
            .await
            .unwrap();

        // T
        TEST_SERVER
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
        let mut server = TEST_SERVER.clone();
        server.spec.config.skip_agent_download = true;
        server.spec.config.plugins = None;
        let context = super::GameServerBuilderContext {
            cluster: &TEST_CLUSTER,
            agent_config: &AgentConfig {
                maven_repository: constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
                version: constants::SHULKER_PLUGIN_VERSION.to_string(),
            },
        };

        // W
        let plugin_urls =
            super::GameServerBuilder::get_plugin_urls(&resourceref_resolver, &context, &server)
                .await
                .unwrap();

        // T
        assert!(plugin_urls.is_empty())
    }
}

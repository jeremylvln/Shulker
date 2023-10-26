use std::collections::BTreeMap;

use k8s_openapi::api::core::v1::Capabilities;
use k8s_openapi::api::core::v1::ConfigMapVolumeSource;
use k8s_openapi::api::core::v1::Container;
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
use shulker_crds::v1alpha1::minecraft_server::MinecraftServerVersion;

use crate::reconcilers::builder::ResourceBuilder;
use crate::resources::resourceref_resolver::ResourceRefResolver;
use google_agones_crds::v1::game_server::GameServer;
use google_agones_crds::v1::game_server::GameServerEvictionSpec;
use google_agones_crds::v1::game_server::GameServerHealthSpec;
use google_agones_crds::v1::game_server::GameServerPortSpec;
use google_agones_crds::v1::game_server::GameServerSpec;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServer;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServerSpec;

use super::config_map::ConfigMapBuilder;
use super::MinecraftServerReconciler;

const MINECRAFT_SERVER_SHULKER_CONFIG_DIR: &str = "/mnt/shulker/config";
const MINECRAFT_SERVER_CONFIG_DIR: &str = "/config";
const MINECRAFT_SERVER_DATA_DIR: &str = "/data";
const MINECRAFT_SERVER_SHULKER_MAVEN_REPOSITORY: &str =
    "https://maven.jeremylvln.fr/artifactory/shulker";

#[cfg(not(test))]
const MINECRAFT_SERVER_SHULKER_PROXY_AGENT_VERSION: &str = env!("CARGO_PKG_VERSION");
#[cfg(test)]
const MINECRAFT_SERVER_SHULKER_PROXY_AGENT_VERSION: &str = "0.0.0-test-cfg";

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

#[async_trait::async_trait]
impl ResourceBuilder for GameServerBuilder {
    type OwnerType = MinecraftServer;
    type ResourceType = GameServer;

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
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let game_server = GameServer {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(minecraft_server.namespace().unwrap().clone()),
                labels: Some(
                    MinecraftServerReconciler::get_common_labels(minecraft_server)
                        .into_iter()
                        .collect(),
                ),
                ..ObjectMeta::default()
            },
            spec: Self::get_game_server_spec(&self.resourceref_resolver, minecraft_server).await?,
            status: None,
        };

        Ok(game_server)
    }
}

impl GameServerBuilder {
    pub fn new(client: Client) -> Self {
        GameServerBuilder {
            client: client.clone(),
            resourceref_resolver: ResourceRefResolver::new(client.clone()),
        }
    }

    pub async fn get_game_server_spec(
        resourceref_resolver: &ResourceRefResolver,
        minecraft_server: &MinecraftServer,
    ) -> Result<GameServerSpec, anyhow::Error> {
        let pod_template_spec =
            Self::get_pod_template_spec(resourceref_resolver, minecraft_server).await?;

        let game_server_spec = GameServerSpec {
            ports: Some(vec![GameServerPortSpec {
                name: "minecraft".to_string(),
                container_port: 25565,
                protocol: "TCP".to_string(),
            }]),
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
                env: Some(Self::get_init_env(resourceref_resolver, &minecraft_server.spec).await?),
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
                image: Some("itzg/minecraft-server:2022.16.0-java17".to_string()),
                name: "minecraft-server".to_string(),
                env: Some(Self::get_env(&minecraft_server.spec)),
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
            service_account_name: Some(format!(
                "{}-server",
                &minecraft_server.spec.cluster_ref.name
            )),
            restart_policy: Some("Never".to_string()),
            volumes: Some(vec![
                Volume {
                    name: "shulker-config".to_string(),
                    config_map: Some(ConfigMapVolumeSource {
                        name: Some(ConfigMapBuilder::name(minecraft_server)),
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
                pod_spec.containers[0].image = Some(image_overrides.name.clone());
                pod_spec.containers[0].image_pull_policy =
                    Some(image_overrides.pull_policy.clone());

                if let Some(image_pull_secrets) = pod_spec.image_pull_secrets.as_mut() {
                    image_pull_secrets.append(&mut image_overrides.image_pull_secrets.clone());
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
        }

        Ok(PodTemplateSpec {
            metadata: Some(ObjectMeta {
                labels: Some(MinecraftServerReconciler::get_common_labels(
                    minecraft_server,
                )),
                annotations: Some(BTreeMap::<String, String>::from([(
                    "minecraftserver.shulkermc.io/tags".to_string(),
                    minecraft_server.spec.tags.join(","),
                )])),
                ..ObjectMeta::default()
            }),
            spec: Some(pod_spec),
        })
    }

    async fn get_init_env(
        resourceref_resolver: &ResourceRefResolver,
        spec: &MinecraftServerSpec,
    ) -> Result<Vec<EnvVar>, anyhow::Error> {
        let mut env: Vec<EnvVar> = vec![
            EnvVar {
                name: "SHULKER_CONFIG_DIR".to_string(),
                value: Some(MINECRAFT_SERVER_SHULKER_CONFIG_DIR.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SERVER_CONFIG_DIR".to_string(),
                value: Some(MINECRAFT_SERVER_CONFIG_DIR.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SERVER_DATA_DIR".to_string(),
                value: Some(MINECRAFT_SERVER_DATA_DIR.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "TYPE".to_string(),
                value: Some(Self::get_type_from_version_channel(&spec.version.channel)),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_SERVER_AGENT_VERSION".to_string(),
                value: Some(MINECRAFT_SERVER_SHULKER_PROXY_AGENT_VERSION.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_MAVEN_REPOSITORY".to_string(),
                value: Some(MINECRAFT_SERVER_SHULKER_MAVEN_REPOSITORY.to_string()),
                ..EnvVar::default()
            },
        ];

        if let Some(world) = &spec.config.world {
            let url = resourceref_resolver.resolve(world).await?.as_url()?;

            env.push(EnvVar {
                name: "SERVER_WORLD_URL".to_string(),
                value: Some(url.to_string()),
                ..EnvVar::default()
            })
        }

        if let Some(plugins) = &spec.config.plugins {
            let urls: Vec<String> = resourceref_resolver
                .resolve_all(plugins)
                .await?
                .into_iter()
                .map(|url| url.to_string())
                .collect();

            env.push(EnvVar {
                name: "SERVER_PLUGIN_URLS".to_string(),
                value: Some(urls.join(",")),
                ..EnvVar::default()
            })
        }

        if let Some(patches) = &spec.config.patches {
            let urls: Vec<String> = resourceref_resolver
                .resolve_all(patches)
                .await?
                .into_iter()
                .map(|url| url.to_string())
                .collect();

            env.push(EnvVar {
                name: "SERVER_PATCH_URLS".to_string(),
                value: Some(urls.join(",")),
                ..EnvVar::default()
            })
        }

        Ok(env)
    }

    fn get_env(spec: &MinecraftServerSpec) -> Vec<EnvVar> {
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
                        name: Some(format!("{}-forwarding-secret", spec.cluster_ref.name)),
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

        if let Some(pod_overrides) = &spec.pod_overrides {
            if let Some(env_overrides) = &pod_overrides.env {
                env.extend(env_overrides.clone());
            }
        }

        env
    }

    fn get_type_from_version_channel(channel: &MinecraftServerVersion) -> String {
        match channel {
            MinecraftServerVersion::Paper => "PAPER".to_string(),
            MinecraftServerVersion::Bukkit => "BUKKIT".to_string(),
            MinecraftServerVersion::Spigot => "SPIGOT".to_string(),
        }
    }

    fn get_version_env_from_version_channel(_channel: &MinecraftServerVersion) -> String {
        "VERSION".to_string()
    }
}

#[cfg(test)]
mod tests {
    use k8s_openapi::api::core::v1::EnvVar;

    use crate::{
        reconcilers::{
            builder::ResourceBuilder,
            minecraft_server::fixtures::{create_client_mock, TEST_SERVER},
        },
        resources::resourceref_resolver::ResourceRefResolver,
    };

    #[test]
    fn name_contains_server_name() {
        // W
        let name = super::GameServerBuilder::name(&TEST_SERVER);

        // T
        assert_eq!(name, "my-server");
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::GameServerBuilder::new(client);

        // W
        let game_server = builder
            .build(&TEST_SERVER, "my-server", None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(game_server);
    }

    #[tokio::test]
    async fn get_init_env_contains_world() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let spec = TEST_SERVER.spec.clone();

        // W
        let env = super::GameServerBuilder::get_init_env(&resourceref_resolver, &spec)
            .await
            .unwrap();

        // T
        let world_env = env
            .iter()
            .find(|env| env.name == "SERVER_WORLD_URL")
            .unwrap();
        assert_eq!(
            world_env,
            &EnvVar {
                name: "SERVER_WORLD_URL".to_string(),
                value: Some("https://example.com/my_world.tar.gz".to_string()),
                ..EnvVar::default()
            }
        );
    }

    #[tokio::test]
    async fn get_init_env_contains_plugins() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let spec = TEST_SERVER.spec.clone();

        // W
        let env = super::GameServerBuilder::get_init_env(&resourceref_resolver, &spec)
            .await
            .unwrap();

        // T
        let plugins_env = env
            .iter()
            .find(|env| env.name == "SERVER_PLUGIN_URLS")
            .unwrap();
        assert_eq!(
            plugins_env,
            &EnvVar {
                name: "SERVER_PLUGIN_URLS".to_string(),
                value: Some("https://example.com/my_plugin.jar".to_string()),
                ..EnvVar::default()
            }
        );
    }

    #[tokio::test]
    async fn get_init_env_contains_patches() {
        // G
        let client = create_client_mock();
        let resourceref_resolver = ResourceRefResolver::new(client);
        let spec = TEST_SERVER.spec.clone();

        // W
        let env = super::GameServerBuilder::get_init_env(&resourceref_resolver, &spec)
            .await
            .unwrap();

        // T
        let patches_env = env
            .iter()
            .find(|env| env.name == "SERVER_PATCH_URLS")
            .unwrap();
        assert_eq!(
            patches_env,
            &EnvVar {
                name: "SERVER_PATCH_URLS".to_string(),
                value: Some("https://example.com/my_patch.tar.gz".to_string()),
                ..EnvVar::default()
            }
        );
    }

    #[test]
    fn get_env_merges_env_overrides() {
        // G
        let spec = TEST_SERVER.spec.clone();

        // W
        let env = super::GameServerBuilder::get_env(&spec);

        // T
        spec.pod_overrides
            .unwrap()
            .env
            .unwrap()
            .iter()
            .for_each(|env_override| {
                assert!(env.contains(env_override));
            });
    }
}

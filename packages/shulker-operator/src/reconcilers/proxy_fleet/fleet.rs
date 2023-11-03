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

use crate::reconcilers::redis_ref::RedisRef;
use crate::resources::resourceref_resolver::ResourceRefResolver;
use google_agones_crds::v1::fleet::Fleet;
use google_agones_crds::v1::fleet::FleetSpec;
use google_agones_crds::v1::game_server::GameServerEvictionSpec;
use google_agones_crds::v1::game_server::GameServerHealthSpec;
use google_agones_crds::v1::game_server::GameServerSpec;
use shulker_crds::v1alpha1::proxy_fleet::ProxyFleet;
use shulker_crds::v1alpha1::proxy_fleet::ProxyFleetTemplateSpec;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

use super::config_map::ConfigMapBuilder;
use super::ProxyFleetReconciler;

const PROXY_IMAGE: &str = "itzg/bungeecord:java17-2022.4.1";
const PROXY_SHULKER_CONFIG_DIR: &str = "/mnt/shulker/config";
const PROXY_SHULKER_FORWARDING_SECRET_DIR: &str = "/mnt/shulker/forwarding-secret";
const PROXY_DATA_DIR: &str = "/server";
const PROXY_DRAIN_LOCK_DIR: &str = "/mnt/drain-lock";
const PROXY_SHULKER_MAVEN_REPOSITORY: &str = "https://maven.jeremylvln.fr/artifactory/shulker";

#[cfg(not(test))]
const PROXY_SHULKER_PROXY_AGENT_VERSION: &str = env!("CARGO_PKG_VERSION");
#[cfg(test)]
const PROXY_SHULKER_PROXY_AGENT_VERSION: &str = "0.0.0-test-cfg";

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
            .get_game_server_spec(context.unwrap().cluster, proxy_fleet)
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

impl FleetBuilder {
    pub fn new(client: Client) -> Self {
        FleetBuilder {
            client: client.clone(),
            resourceref_resolver: ResourceRefResolver::new(client.clone()),
        }
    }

    async fn get_game_server_spec(
        &self,
        cluster: &MinecraftCluster,
        proxy_fleet: &ProxyFleet,
    ) -> Result<GameServerSpec, anyhow::Error> {
        let pod_template_spec = self.get_pod_template_spec(cluster, proxy_fleet).await?;
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
        cluster: &MinecraftCluster,
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
                env: Some(self.get_init_env(proxy_fleet).await?),
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
                image: Some(PROXY_IMAGE.to_string()),
                name: "proxy".to_string(),
                ports: Some(vec![ContainerPort {
                    name: Some("minecraft".to_string()),
                    container_port: 25577,
                    ..ContainerPort::default()
                }]),
                env: Some(self.get_env(cluster, &proxy_fleet.spec.template.spec)?),
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
                volume_mounts: Some(vec![
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
                ]),
                ..Container::default()
            }],
            service_account_name: Some(format!(
                "shulker-{}-proxy",
                &proxy_fleet.spec.cluster_ref.name
            )),
            restart_policy: Some("Never".to_string()),
            volumes: Some(vec![
                Volume {
                    name: "shulker-config".to_string(),
                    config_map: Some(ConfigMapVolumeSource {
                        name: Some(ConfigMapBuilder::name(proxy_fleet)),
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
            ]),
            ..PodSpec::default()
        };

        if let Some(pod_overrides) = &proxy_fleet.spec.template.spec.pod_overrides {
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

        let mut pod_labels =
            ProxyFleetReconciler::get_labels(proxy_fleet, "proxy".to_string(), "proxy".to_string());
        let mut pod_annotations = BTreeMap::<String, String>::from([(
            "kubectl.kubernetes.io/default-exec-container".to_string(),
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

    async fn get_init_env(&self, proxy_fleet: &ProxyFleet) -> Result<Vec<EnvVar>, anyhow::Error> {
        let spec = &proxy_fleet.spec.template.spec;

        let mut env: Vec<EnvVar> = vec![
            EnvVar {
                name: "SHULKER_CONFIG_DIR".to_string(),
                value: Some(PROXY_SHULKER_CONFIG_DIR.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "PROXY_DATA_DIR".to_string(),
                value: Some(PROXY_DATA_DIR.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "TYPE".to_string(),
                value: Some(Self::get_type_from_version_channel(&spec.version.channel)),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_PROXY_AGENT_VERSION".to_string(),
                value: Some(PROXY_SHULKER_PROXY_AGENT_VERSION.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_MAVEN_REPOSITORY".to_string(),
                value: Some(PROXY_SHULKER_MAVEN_REPOSITORY.to_string()),
                ..EnvVar::default()
            },
        ];

        if let Some(plugins) = &spec.config.plugins {
            let urls: Vec<String> = self
                .resourceref_resolver
                .resolve_all(proxy_fleet.namespace().as_ref().unwrap(), plugins)
                .await?
                .into_iter()
                .map(|url| url.to_string())
                .collect();

            env.push(EnvVar {
                name: "PROXY_PLUGIN_URLS".to_string(),
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
                name: "PROXY_PATCH_URLS".to_string(),
                value: Some(urls.join(";")),
                ..EnvVar::default()
            })
        }

        Ok(env)
    }

    fn get_env(
        &self,
        cluster: &MinecraftCluster,
        spec: &ProxyFleetTemplateSpec,
    ) -> Result<Vec<EnvVar>, anyhow::Error> {
        let redis_ref = RedisRef::from_cluster(cluster)?;

        let mut env: Vec<EnvVar> = vec![
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
                name: "SHULKER_PROXY_TTL_SECONDS".to_string(),
                value: Some(spec.config.ttl_seconds.to_string()),
                ..EnvVar::default()
            },
            EnvVar {
                name: "SHULKER_NETWORK_ADMINS".to_string(),
                value: Some(
                    cluster
                        .spec
                        .network_admins
                        .as_ref()
                        .map(|list| list.join(","))
                        .unwrap_or("".to_string()),
                ),
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
}

#[cfg(test)]
mod tests {
    use k8s_openapi::api::core::v1::EnvVar;
    use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

    use crate::reconcilers::{
        minecraft_cluster::fixtures::TEST_CLUSTER,
        proxy_fleet::fixtures::{create_client_mock, TEST_PROXY_FLEET},
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
    async fn get_init_env_contains_plugins() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);

        // W
        let env = builder.get_init_env(&TEST_PROXY_FLEET).await.unwrap();

        // T
        let plugins_env = env
            .iter()
            .find(|env| env.name == "PROXY_PLUGIN_URLS")
            .unwrap();
        assert_eq!(
            plugins_env,
            &EnvVar {
                name: "PROXY_PLUGIN_URLS".to_string(),
                value: Some("https://example.com/my_plugin.jar".to_string()),
                ..EnvVar::default()
            }
        );
    }

    #[tokio::test]
    async fn get_init_env_contains_patches() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);

        // W
        let env = builder.get_init_env(&TEST_PROXY_FLEET).await.unwrap();

        // T
        let patches_env = env
            .iter()
            .find(|env| env.name == "PROXY_PATCH_URLS")
            .unwrap();
        assert_eq!(
            patches_env,
            &EnvVar {
                name: "PROXY_PATCH_URLS".to_string(),
                value: Some("https://example.com/my_patch.tar.gz".to_string()),
                ..EnvVar::default()
            }
        );
    }

    #[tokio::test]
    async fn get_env_merges_env_overrides() {
        // G
        let client = create_client_mock();
        let builder = super::FleetBuilder::new(client);
        let spec = TEST_PROXY_FLEET.spec.clone();

        // W
        let env = builder.get_env(&TEST_CLUSTER, &spec.template.spec).unwrap();

        // T
        spec.template
            .spec
            .pod_overrides
            .unwrap()
            .env
            .unwrap()
            .iter()
            .for_each(|env_override| {
                assert!(env.contains(env_override));
            });
    }
}

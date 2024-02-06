use std::collections::BTreeMap;

use k8s_openapi::api::apps::v1::StatefulSet;
use k8s_openapi::api::apps::v1::StatefulSetSpec;
use k8s_openapi::api::core::v1::Capabilities;
use k8s_openapi::api::core::v1::Container;
use k8s_openapi::api::core::v1::ContainerPort;
use k8s_openapi::api::core::v1::EnvVar;
use k8s_openapi::api::core::v1::PersistentVolumeClaim;
use k8s_openapi::api::core::v1::PersistentVolumeClaimSpec;
use k8s_openapi::api::core::v1::PodSpec;
use k8s_openapi::api::core::v1::PodTemplateSpec;
use k8s_openapi::api::core::v1::SecurityContext;
use k8s_openapi::api::core::v1::VolumeMount;
use k8s_openapi::api::core::v1::VolumeResourceRequirements;
use k8s_openapi::apimachinery::pkg::api::resource::Quantity;
use k8s_openapi::apimachinery::pkg::apis::meta::v1::LabelSelector;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;
use lazy_static::lazy_static;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftClusterRedisDeploymentType;

use super::redis_service::RedisServiceBuilder;
use super::MinecraftClusterReconciler;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

const REDIS_IMAGE: &str = "redis:7-alpine";
const REDIS_DATA_DIR: &str = "/data";

lazy_static! {
    static ref REDIS_SECURITY_CONTEXT: SecurityContext = SecurityContext {
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

pub struct RedisStatefulSetBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl<'a> ResourceBuilder<'a> for RedisStatefulSetBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = StatefulSet;
    type Context = ();

    fn name(cluster: &Self::OwnerType) -> String {
        format!("{}-redis-managed", cluster.name_any())
    }

    fn api(&self, cluster: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(self.client.clone(), cluster.namespace().as_ref().unwrap())
    }

    fn is_needed(&self, cluster: &Self::OwnerType) -> bool {
        cluster.spec.redis.as_ref().map_or(true, |redis| {
            redis.type_ == MinecraftClusterRedisDeploymentType::ManagedSingleNode
        })
    }

    async fn build(
        &self,
        cluster: &Self::OwnerType,
        name: &str,
        _existing_stateful_set: Option<&Self::ResourceType>,
        _context: Option<Self::Context>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let stateful_set = StatefulSet {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(cluster.namespace().unwrap().clone()),
                labels: Some(MinecraftClusterReconciler::get_labels(
                    cluster,
                    "redis".to_string(),
                    "redis".to_string(),
                )),
                ..ObjectMeta::default()
            },
            spec: Some(StatefulSetSpec {
                selector: LabelSelector {
                    match_labels: Some(MinecraftClusterReconciler::get_labels(
                        cluster,
                        "redis".to_string(),
                        "redis".to_string(),
                    )),
                    ..LabelSelector::default()
                },
                service_name: RedisServiceBuilder::name(cluster),
                replicas: Some(1),
                template: RedisStatefulSetBuilder::get_pod_template_spec(cluster),
                volume_claim_templates: Some(RedisStatefulSetBuilder::get_volume_claim_templates()),
                ..StatefulSetSpec::default()
            }),
            ..StatefulSet::default()
        };

        Ok(stateful_set)
    }
}

impl RedisStatefulSetBuilder {
    pub fn new(client: Client) -> Self {
        RedisStatefulSetBuilder { client }
    }

    fn get_pod_template_spec(cluster: &MinecraftCluster) -> PodTemplateSpec {
        let pod_spec = PodSpec {
            containers: vec![Container {
                image: Some(REDIS_IMAGE.to_string()),
                name: "redis".to_string(),
                command: Some(vec![
                    "redis-server".to_string(),
                    format!("--dir {}", REDIS_DATA_DIR),
                ]),
                env: Some(vec![EnvVar {
                    name: "MASTER".to_string(),
                    value: Some("true".to_string()),
                    ..EnvVar::default()
                }]),
                ports: Some(vec![ContainerPort {
                    name: Some("redis".to_string()),
                    container_port: 6379,
                    ..ContainerPort::default()
                }]),
                security_context: Some(REDIS_SECURITY_CONTEXT.clone()),
                volume_mounts: Some(vec![VolumeMount {
                    name: "data".to_string(),
                    mount_path: REDIS_DATA_DIR.to_string(),
                    ..VolumeMount::default()
                }]),
                ..Container::default()
            }],
            ..PodSpec::default()
        };

        PodTemplateSpec {
            metadata: Some(ObjectMeta {
                labels: Some(MinecraftClusterReconciler::get_labels(
                    cluster,
                    "redis".to_string(),
                    "redis".to_string(),
                )),
                ..ObjectMeta::default()
            }),
            spec: Some(pod_spec),
        }
    }

    fn get_volume_claim_templates() -> Vec<PersistentVolumeClaim> {
        vec![PersistentVolumeClaim {
            metadata: ObjectMeta {
                name: Some("data".to_string()),
                ..ObjectMeta::default()
            },
            spec: Some(PersistentVolumeClaimSpec {
                access_modes: Some(vec!["ReadWriteOnce".to_string()]),
                resources: Some(VolumeResourceRequirements {
                    requests: Some(BTreeMap::from([(
                        "storage".to_string(),
                        Quantity("1Gi".to_string()),
                    )])),
                    ..VolumeResourceRequirements::default()
                }),
                ..PersistentVolumeClaimSpec::default()
            }),
            ..PersistentVolumeClaim::default()
        }]
    }
}

#[cfg(test)]
mod tests {
    use shulker_crds::v1alpha1::minecraft_cluster::{
        MinecraftClusterRedisDeploymentType, MinecraftClusterRedisSpec,
    };
    use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

    use crate::reconcilers::minecraft_cluster::fixtures::{create_client_mock, TEST_CLUSTER};

    #[test]
    fn name_contains_cluster_name() {
        // W
        let name = super::RedisStatefulSetBuilder::name(&TEST_CLUSTER);

        // T
        assert_eq!(name, "my-cluster-redis-managed");
    }

    #[tokio::test]
    async fn is_needed_without_config() {
        // G
        let client = create_client_mock();
        let builder = super::RedisStatefulSetBuilder::new(client);

        // W
        let is_needed = builder.is_needed(&TEST_CLUSTER);

        // T
        assert!(is_needed);
    }

    #[tokio::test]
    async fn is_needed_with_managed_type() {
        // G
        let client = create_client_mock();
        let builder = super::RedisStatefulSetBuilder::new(client);
        let mut cluster = TEST_CLUSTER.clone();
        cluster.spec.redis = Some(MinecraftClusterRedisSpec {
            type_: MinecraftClusterRedisDeploymentType::ManagedSingleNode,
            ..MinecraftClusterRedisSpec::default()
        });

        // W
        let is_needed = builder.is_needed(&cluster);

        // T
        assert!(is_needed);
    }

    #[tokio::test]
    async fn is_needed_with_provided_type() {
        // G
        let client = create_client_mock();
        let builder = super::RedisStatefulSetBuilder::new(client);
        let mut cluster = TEST_CLUSTER.clone();
        cluster.spec.redis = Some(MinecraftClusterRedisSpec {
            type_: MinecraftClusterRedisDeploymentType::Provided,
            ..MinecraftClusterRedisSpec::default()
        });

        // W
        let is_needed = builder.is_needed(&cluster);

        // T
        assert!(!is_needed);
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::RedisStatefulSetBuilder::new(client);
        let name = super::RedisStatefulSetBuilder::name(&TEST_CLUSTER);

        // W
        let role = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(role);
    }
}

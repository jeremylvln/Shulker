use k8s_openapi::api::core::v1::Service;
use k8s_openapi::api::core::v1::ServicePort;
use k8s_openapi::api::core::v1::ServiceSpec;
use k8s_openapi::apimachinery::pkg::util::intstr::IntOrString;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftClusterRedisDeploymentType;

use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

use super::MinecraftClusterReconciler;

pub struct RedisServiceBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl<'a> ResourceBuilder<'a> for RedisServiceBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = Service;
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
        _existing_service: Option<&Self::ResourceType>,
        _context: Option<Self::Context>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let service = Service {
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
            spec: Some(ServiceSpec {
                selector: Some(MinecraftClusterReconciler::get_labels(
                    cluster,
                    "redis".to_string(),
                    "redis".to_string(),
                )),
                type_: Some("ClusterIP".to_string()),
                ports: Some(vec![ServicePort {
                    name: Some("redis".to_string()),
                    protocol: Some("TCP".to_string()),
                    port: 6379,
                    target_port: Some(IntOrString::String("redis".to_string())),
                    ..ServicePort::default()
                }]),
                ..ServiceSpec::default()
            }),
            ..Service::default()
        };

        Ok(service)
    }
}

impl RedisServiceBuilder {
    pub fn new(client: Client) -> Self {
        RedisServiceBuilder { client }
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
    fn name_contains_fleet_name() {
        // W
        let name = super::RedisServiceBuilder::name(&TEST_CLUSTER);

        // T
        assert_eq!(name, "my-cluster-redis-managed");
    }

    #[tokio::test]
    async fn is_needed_without_config() {
        // G
        let client = create_client_mock();
        let builder = super::RedisServiceBuilder::new(client);

        // W
        let is_needed = builder.is_needed(&TEST_CLUSTER);

        // T
        assert!(is_needed);
    }

    #[tokio::test]
    async fn is_needed_with_managed_type() {
        // G
        let client = create_client_mock();
        let builder = super::RedisServiceBuilder::new(client);
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
        let builder = super::RedisServiceBuilder::new(client);
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
        let builder = super::RedisServiceBuilder::new(client);
        let name = super::RedisServiceBuilder::name(&TEST_CLUSTER);

        // W
        let service = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(service);
    }
}

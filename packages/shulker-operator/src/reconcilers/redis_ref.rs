use shulker_crds::v1alpha1::minecraft_cluster::{
    MinecraftCluster, MinecraftClusterRedisDeploymentType,
};
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

use super::minecraft_cluster::redis_service::RedisServiceBuilder;

#[derive(Debug, PartialEq, Clone)]
pub struct RedisRef {
    pub host: String,
    pub port: u16,
    pub credentials_secret_name: Option<String>,
}

impl RedisRef {
    pub fn from_cluster(cluster: &MinecraftCluster) -> Result<Self, anyhow::Error> {
        let is_managed = cluster.spec.redis.as_ref().map_or(true, |r| {
            r.type_ == MinecraftClusterRedisDeploymentType::ManagedSingleNode
        });

        match is_managed {
            true => Ok(RedisRef {
                host: RedisServiceBuilder::name(cluster),
                port: 6379,
                credentials_secret_name: None,
            }),
            false => match cluster.spec.redis.as_ref().unwrap().provided.as_ref() {
                Some(provided_spec) => Ok(RedisRef {
                    host: provided_spec.host.clone(),
                    port: provided_spec.port,
                    credentials_secret_name: provided_spec.credentials_secret_name.clone(),
                }),
                None => Err(anyhow::anyhow!(
                    "Redis is not managed and no provided spec was found"
                )),
            },
        }
    }
}

#[cfg(test)]
mod tests {
    use shulker_crds::v1alpha1::minecraft_cluster::{
        MinecraftClusterRedisDeploymentType, MinecraftClusterRedisProvidedSpec,
        MinecraftClusterRedisSpec,
    };

    use crate::reconcilers::{minecraft_cluster::fixtures::TEST_CLUSTER, redis_ref::RedisRef};

    #[test]
    fn from_cluster_default() {
        // G
        let mut cluster = TEST_CLUSTER.clone();
        cluster.spec.redis = None;

        // W
        let redis_ref = super::RedisRef::from_cluster(&cluster).unwrap();

        // T
        assert_eq!(
            redis_ref,
            RedisRef {
                host: "my-cluster-redis-managed".to_string(),
                port: 6379,
                credentials_secret_name: None,
            }
        );
    }

    #[test]
    fn from_cluster_managed() {
        // G
        let mut cluster = TEST_CLUSTER.clone();
        cluster.spec.redis = Some(MinecraftClusterRedisSpec {
            type_: MinecraftClusterRedisDeploymentType::ManagedSingleNode,
            provided: None,
        });

        // W
        let redis_ref = super::RedisRef::from_cluster(&cluster).unwrap();

        // T
        assert_eq!(
            redis_ref,
            RedisRef {
                host: "my-cluster-redis-managed".to_string(),
                port: 6379,
                credentials_secret_name: None,
            }
        );
    }

    #[test]
    fn from_cluster_provided() {
        // G
        let mut cluster = TEST_CLUSTER.clone();
        cluster.spec.redis = Some(MinecraftClusterRedisSpec {
            type_: MinecraftClusterRedisDeploymentType::Provided,
            provided: Some(MinecraftClusterRedisProvidedSpec {
                host: "my-redis-host".to_string(),
                port: 1234,
                credentials_secret_name: Some("my-redis-credentials".to_string()),
            }),
        });

        // W
        let redis_ref = super::RedisRef::from_cluster(&cluster).unwrap();

        // T
        assert_eq!(
            redis_ref,
            RedisRef {
                host: "my-redis-host".to_string(),
                port: 1234,
                credentials_secret_name: Some("my-redis-credentials".to_string()),
            }
        );
    }
}

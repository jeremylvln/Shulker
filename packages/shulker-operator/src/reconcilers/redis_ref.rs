use shulker_crds::v1alpha1::minecraft_cluster::{
    MinecraftCluster, MinecraftClusterRedisDeploymentType,
};
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

use super::minecraft_cluster::redis_service::RedisServiceBuilder;

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

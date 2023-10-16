use std::collections::BTreeMap;

use k8s_openapi::api::core::v1::Secret;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;
use rand::distributions::{Alphanumeric, DistString};

use crate::reconcilers::builder::ResourceBuilder;

use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;

use super::MinecraftClusterReconciler;

const SECRET_DATA_KEY: &str = "key";

pub struct ForwardingSecretBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder for ForwardingSecretBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = Secret;

    fn name(cluster: &Self::OwnerType) -> String {
        format!("{}-forwarding-secret", cluster.name_any())
    }

    fn is_updatable() -> bool {
        false
    }

    fn api(&self, cluster: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(self.client.clone(), cluster.namespace().as_ref().unwrap())
    }

    fn is_needed(&self, _cluster: &Self::OwnerType) -> bool {
        true
    }

    async fn create(
        &self,
        cluster: &Self::OwnerType,
        name: &str,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let secret = Secret {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(cluster.namespace().unwrap().clone()),
                labels: Some(
                    MinecraftClusterReconciler::get_common_labels(cluster)
                        .into_iter()
                        .collect(),
                ),
                ..ObjectMeta::default()
            },
            type_: Some("Opaque".to_string()),
            string_data: Some(BTreeMap::from([(
                SECRET_DATA_KEY.to_string(),
                Self::create_proxy_guard_key(),
            )])),
            ..Secret::default()
        };

        Ok(secret)
    }

    async fn update(
        &self,
        _cluster: &Self::OwnerType,
        _secret: &mut Self::ResourceType,
    ) -> Result<(), anyhow::Error> {
        Ok(())
    }
}

impl ForwardingSecretBuilder {
    pub fn new(client: Client) -> Self {
        ForwardingSecretBuilder { client }
    }

    fn create_proxy_guard_key() -> String {
        let mut rng = rand::thread_rng();
        Alphanumeric.sample_string(&mut rng, 64)
    }
}

#[cfg(test)]
mod tests {
    use crate::reconcilers::{
        builder::ResourceBuilder,
        minecraft_cluster::fixtures::{create_client_mock, TEST_CLUSTER},
    };

    #[test]
    fn name_contains_cluster_name() {
        // W
        let name = super::ForwardingSecretBuilder::name(&TEST_CLUSTER);

        // T
        assert_eq!(name, "my-cluster-forwarding-secret");
    }

    #[tokio::test]
    async fn create_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::ForwardingSecretBuilder::new(client);

        // W
        let secret = builder
            .create(&TEST_CLUSTER, "my-cluster-forwarding-secret")
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(secret, {
            ".stringData.key" => "[forwarding secret random redacted]"
        });
    }

    #[tokio::test]
    async fn create_has_secret() {
        // G
        let client = create_client_mock();
        let builder = super::ForwardingSecretBuilder::new(client);

        // W
        let secret = builder
            .create(&TEST_CLUSTER, "my-cluster-forwarding-secret")
            .await
            .unwrap();

        // T
        assert!(secret
            .string_data
            .unwrap()
            .get(super::SECRET_DATA_KEY)
            .is_some());
    }

    #[test]
    fn create_proxy_guard_key_random() {
        // W
        let (secret_1, secret_2, secret_3) = (
            super::ForwardingSecretBuilder::create_proxy_guard_key(),
            super::ForwardingSecretBuilder::create_proxy_guard_key(),
            super::ForwardingSecretBuilder::create_proxy_guard_key(),
        );

        // T
        assert_ne!(secret_1, secret_2);
        assert_ne!(secret_2, secret_3);
        assert_ne!(secret_3, secret_1);
    }
}

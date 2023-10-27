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
impl<'a> ResourceBuilder<'a> for ForwardingSecretBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = Secret;
    type Context = ();

    fn name(cluster: &Self::OwnerType) -> String {
        format!("{}-forwarding-secret", cluster.name_any())
    }

    fn api(&self, cluster: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(self.client.clone(), cluster.namespace().as_ref().unwrap())
    }

    async fn build(
        &self,
        cluster: &Self::OwnerType,
        name: &str,
        existing_secret: Option<&Self::ResourceType>,
        _context: Option<Self::Context>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let secret = Secret {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(cluster.namespace().unwrap().clone()),
                labels: Some(MinecraftClusterReconciler::get_labels(
                    cluster,
                    "forwarding-secret".to_string(),
                    "proxy".to_string(),
                )),
                ..ObjectMeta::default()
            },
            type_: Some("Opaque".to_string()),
            string_data: Some(BTreeMap::from([(
                SECRET_DATA_KEY.to_string(),
                Self::get_existing_or_new_forwarding_secret(existing_secret),
            )])),
            ..Secret::default()
        };

        Ok(secret)
    }
}

impl ForwardingSecretBuilder {
    pub fn new(client: Client) -> Self {
        ForwardingSecretBuilder { client }
    }

    fn get_existing_or_new_forwarding_secret(existing_secret: Option<&Secret>) -> String {
        match existing_secret {
            Some(existing_secret) => {
                if existing_secret.data.is_some() {
                    existing_secret
                        .data
                        .as_ref()
                        .unwrap()
                        .get(SECRET_DATA_KEY)
                        .cloned()
                        .map(|key| String::from_utf8(key.0).unwrap())
                        .unwrap_or_else(ForwardingSecretBuilder::create_forwarding_secret)
                } else if existing_secret.string_data.is_some() {
                    existing_secret
                        .string_data
                        .as_ref()
                        .unwrap()
                        .get(SECRET_DATA_KEY)
                        .cloned()
                        .unwrap_or_else(ForwardingSecretBuilder::create_forwarding_secret)
                } else {
                    ForwardingSecretBuilder::create_forwarding_secret()
                }
            }
            None => ForwardingSecretBuilder::create_forwarding_secret(),
        }
    }

    fn create_forwarding_secret() -> String {
        let mut rng = rand::thread_rng();
        Alphanumeric.sample_string(&mut rng, 64)
    }
}

#[cfg(test)]
mod tests {
    use std::collections::BTreeMap;

    use k8s_openapi::{api::core::v1::Secret, ByteString};
    use kube::core::ObjectMeta;

    use crate::reconcilers::{
        builder::ResourceBuilder,
        minecraft_cluster::{
            fixtures::{create_client_mock, TEST_CLUSTER},
            forwarding_secret::SECRET_DATA_KEY,
        },
    };

    #[test]
    fn name_contains_cluster_name() {
        // W
        let name = super::ForwardingSecretBuilder::name(&TEST_CLUSTER);

        // T
        assert_eq!(name, "my-cluster-forwarding-secret");
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::ForwardingSecretBuilder::new(client);
        let name = super::ForwardingSecretBuilder::name(&TEST_CLUSTER);

        // W
        let secret = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(secret, {
            ".stringData.key" => "[forwarding secret random redacted]"
        });
    }

    #[tokio::test]
    async fn build_has_secret() {
        // G
        let client = create_client_mock();
        let builder = super::ForwardingSecretBuilder::new(client);
        let name = super::ForwardingSecretBuilder::name(&TEST_CLUSTER);

        // W
        let secret = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        assert!(secret
            .string_data
            .unwrap()
            .get(super::SECRET_DATA_KEY)
            .is_some());
    }

    #[tokio::test]
    async fn build_reuse_existing_forwarding_secret() {
        // G
        let client = create_client_mock();
        let builder = super::ForwardingSecretBuilder::new(client);
        let name = super::ForwardingSecretBuilder::name(&TEST_CLUSTER);
        let existing_secret = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // W
        let secret = builder
            .build(&TEST_CLUSTER, &name, Some(&existing_secret), None)
            .await
            .unwrap();

        // T
        assert_eq!(
            secret
                .string_data
                .unwrap()
                .get(super::SECRET_DATA_KEY)
                .unwrap(),
            existing_secret
                .string_data
                .unwrap()
                .get(super::SECRET_DATA_KEY)
                .unwrap()
        );
    }

    #[test]
    fn get_existing_or_new_forwarding_secret_none() {
        // W
        let key = super::ForwardingSecretBuilder::get_existing_or_new_forwarding_secret(None);

        // T
        assert!(!key.is_empty());
    }

    #[test]
    fn get_existing_or_new_forwarding_secret_missing_key() {
        // G
        let secret = Secret {
            metadata: ObjectMeta::default(),
            data: None,
            string_data: None,
            ..Secret::default()
        };

        // W
        let key =
            super::ForwardingSecretBuilder::get_existing_or_new_forwarding_secret(Some(&secret));

        // T
        assert!(!key.is_empty());
    }

    #[test]
    fn get_existing_or_new_forwarding_secret_exist_data() {
        // G
        let existing_forwarding_secret = "test";
        let secret = Secret {
            metadata: ObjectMeta::default(),
            data: Some(BTreeMap::from([(
                SECRET_DATA_KEY.to_string(),
                ByteString(existing_forwarding_secret.as_bytes().to_vec()),
            )])),
            string_data: None,
            ..Secret::default()
        };

        // W
        let key =
            super::ForwardingSecretBuilder::get_existing_or_new_forwarding_secret(Some(&secret));

        // T
        assert_eq!(key, existing_forwarding_secret);
    }

    #[test]
    fn get_existing_or_new_forwarding_secret_exist_string_data() {
        // G
        let existing_forwarding_secret = "test";
        let secret = Secret {
            metadata: ObjectMeta::default(),
            data: None,
            string_data: Some(BTreeMap::from([(
                SECRET_DATA_KEY.to_string(),
                existing_forwarding_secret.to_string(),
            )])),
            ..Secret::default()
        };

        // W
        let key =
            super::ForwardingSecretBuilder::get_existing_or_new_forwarding_secret(Some(&secret));

        // T
        assert_eq!(key, existing_forwarding_secret);
    }

    #[test]
    fn create_forwarding_secret_random() {
        // W
        let (secret_1, secret_2, secret_3) = (
            super::ForwardingSecretBuilder::create_forwarding_secret(),
            super::ForwardingSecretBuilder::create_forwarding_secret(),
            super::ForwardingSecretBuilder::create_forwarding_secret(),
        );

        // T
        assert_ne!(secret_1, secret_2);
        assert_ne!(secret_2, secret_3);
        assert_ne!(secret_3, secret_1);
    }
}

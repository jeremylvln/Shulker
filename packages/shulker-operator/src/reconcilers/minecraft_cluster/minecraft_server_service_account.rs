use k8s_openapi::api::core::v1::ServiceAccount;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use super::MinecraftClusterReconciler;
use crate::reconcilers::builder::ResourceBuilder;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;

pub struct MinecraftServerServiceAccountBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder for MinecraftServerServiceAccountBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = ServiceAccount;

    fn name(cluster: &Self::OwnerType) -> String {
        format!("{}-server", cluster.name_any())
    }

    fn is_updatable() -> bool {
        true
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
        let service_account = ServiceAccount {
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
            ..ServiceAccount::default()
        };

        Ok(service_account)
    }

    async fn update(
        &self,
        _cluster: &Self::OwnerType,
        _service_account: &mut Self::ResourceType,
    ) -> Result<(), anyhow::Error> {
        Ok(())
    }
}

impl MinecraftServerServiceAccountBuilder {
    pub fn new(client: Client) -> Self {
        MinecraftServerServiceAccountBuilder { client }
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
        let name = super::MinecraftServerServiceAccountBuilder::name(&TEST_CLUSTER);

        // T
        assert_eq!(name, "my-cluster-server");
    }

    #[tokio::test]
    async fn create_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::MinecraftServerServiceAccountBuilder::new(client);

        // W
        let service_account = builder
            .create(&TEST_CLUSTER, "my-cluster-server")
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(service_account);
    }
}

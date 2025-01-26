use k8s_openapi::api::core::v1::ServiceAccount;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use super::MinecraftClusterReconciler;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

pub struct MinecraftServerServiceAccountBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder<'_> for MinecraftServerServiceAccountBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = ServiceAccount;
    type Context = ();

    fn name(cluster: &Self::OwnerType) -> String {
        format!("shulker-{}-server", cluster.name_any())
    }

    fn api(&self, cluster: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(self.client.clone(), cluster.namespace().as_ref().unwrap())
    }

    async fn build(
        &self,
        cluster: &Self::OwnerType,
        name: &str,
        _existing_service_account: Option<&Self::ResourceType>,
        _context: Option<Self::Context>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let service_account = ServiceAccount {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(cluster.namespace().unwrap().clone()),
                labels: Some(MinecraftClusterReconciler::get_labels(
                    cluster,
                    "service-account".to_string(),
                    "minecraft-server-rbac".to_string(),
                )),
                ..ObjectMeta::default()
            },
            ..ServiceAccount::default()
        };

        Ok(service_account)
    }
}

impl MinecraftServerServiceAccountBuilder {
    pub fn new(client: Client) -> Self {
        MinecraftServerServiceAccountBuilder { client }
    }
}

#[cfg(test)]
mod tests {
    use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

    use crate::reconcilers::minecraft_cluster::fixtures::{create_client_mock, TEST_CLUSTER};

    #[test]
    fn name_contains_cluster_name() {
        // W
        let name = super::MinecraftServerServiceAccountBuilder::name(&TEST_CLUSTER);

        // T
        assert_eq!(name, "shulker-my-cluster-server");
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::MinecraftServerServiceAccountBuilder::new(client);
        let name = super::MinecraftServerServiceAccountBuilder::name(&TEST_CLUSTER);

        // W
        let service_account = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(service_account);
    }
}

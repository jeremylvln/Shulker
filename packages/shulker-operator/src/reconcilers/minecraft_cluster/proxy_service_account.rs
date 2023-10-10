use k8s_openapi::api::core::v1::ServiceAccount;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use super::MinecraftClusterReconciler;
use crate::reconcilers::builder::ResourceBuilder;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;

pub struct ProxyServiceAccountBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder for ProxyServiceAccountBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = ServiceAccount;

    fn name(cluster: &Self::OwnerType) -> String {
        format!("{}-proxy", cluster.name_any())
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

impl ProxyServiceAccountBuilder {
    pub fn new(client: Client) -> Self {
        ProxyServiceAccountBuilder { client }
    }
}

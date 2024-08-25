use std::collections::BTreeMap;

use k8s_openapi::api::core::v1::ConfigMap;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

use super::MinecraftClusterReconciler;

pub struct ExternalServersConfigMapBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl<'a> ResourceBuilder<'a> for ExternalServersConfigMapBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = ConfigMap;
    type Context = ();

    fn name(cluster: &Self::OwnerType) -> String {
        format!("{}-external-servers", cluster.name_any())
    }

    fn api(&self, cluster: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(self.client.clone(), cluster.namespace().as_ref().unwrap())
    }

    fn is_needed(&self, cluster: &Self::OwnerType) -> bool {
        cluster
            .spec
            .external_servers
            .as_ref()
            .map_or(false, |list| !list.is_empty())
    }

    async fn build(
        &self,
        cluster: &Self::OwnerType,
        name: &str,
        _existing_config_map: Option<&Self::ResourceType>,
        _context: Option<Self::Context>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let config_map = ConfigMap {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(cluster.namespace().unwrap().clone()),
                labels: Some(MinecraftClusterReconciler::get_labels(
                    cluster,
                    "external-servers".to_string(),
                    "proxy".to_string(),
                )),
                ..ObjectMeta::default()
            },
            data: Some(BTreeMap::from([(
                "external-servers.yaml".to_string(),
                ExternalServersConfigMapBuilder::get_content_from_server_list(cluster),
            )])),
            ..ConfigMap::default()
        };

        Ok(config_map)
    }
}

impl ExternalServersConfigMapBuilder {
    pub fn new(client: Client) -> Self {
        ExternalServersConfigMapBuilder { client }
    }

    fn get_content_from_server_list(cluster: &MinecraftCluster) -> String {
        serde_yaml::to_string(cluster.spec.external_servers.as_ref().unwrap()).unwrap()
    }
}

#[cfg(test)]
mod tests {
    use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

    use crate::reconcilers::minecraft_cluster::fixtures::{create_client_mock, TEST_CLUSTER};

    #[test]
    fn name_contains_cluster_name() {
        // W
        let name = super::ExternalServersConfigMapBuilder::name(&TEST_CLUSTER);

        // T
        assert_eq!(name, "my-cluster-external-servers");
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::ExternalServersConfigMapBuilder::new(client);
        let name = super::ExternalServersConfigMapBuilder::name(&TEST_CLUSTER);

        // W
        let config_map = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(config_map);
    }
}

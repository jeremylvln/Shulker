use std::collections::BTreeMap;

use k8s_openapi::api::core::v1::Service;
use k8s_openapi::api::core::v1::ServiceSpec;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

use super::MinecraftClusterReconciler;

pub struct HeadlessServiceBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder<'_> for HeadlessServiceBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = Service;
    type Context = ();

    fn name(cluster: &Self::OwnerType) -> String {
        format!("{}-cluster", cluster.name_any())
    }

    fn api(&self, cluster: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(self.client.clone(), cluster.namespace().as_ref().unwrap())
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
                    "service".to_string(),
                    "minecraft-server-headless".to_string(),
                )),
                ..ObjectMeta::default()
            },
            spec: Some(ServiceSpec {
                selector: Some(BTreeMap::from([(
                    "minecraftcluster.shulkermc.io/name".to_string(),
                    cluster.name_any(),
                )])),
                type_: None,
                cluster_ip: Some("None".to_string()),
                ports: Some(vec![]),
                ..ServiceSpec::default()
            }),
            ..Service::default()
        };

        Ok(service)
    }
}

impl HeadlessServiceBuilder {
    pub fn new(client: Client) -> Self {
        HeadlessServiceBuilder { client }
    }
}

#[cfg(test)]
mod tests {
    use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

    use crate::reconcilers::minecraft_cluster::fixtures::{create_client_mock, TEST_CLUSTER};

    #[test]
    fn name_contains_cluster_name() {
        // W
        let name = super::HeadlessServiceBuilder::name(&TEST_CLUSTER);

        // T
        assert_eq!(name, "my-cluster-cluster");
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::HeadlessServiceBuilder::new(client);
        let name = super::HeadlessServiceBuilder::name(&TEST_CLUSTER);

        // W
        let service = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(service);
    }
}

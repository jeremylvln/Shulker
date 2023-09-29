use k8s_openapi::api::rbac::v1::RoleBinding;
use k8s_openapi::api::rbac::v1::RoleRef;
use k8s_openapi::api::rbac::v1::Subject;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use super::proxy_role::ProxyRoleBuilder;
use super::proxy_service_account::ProxyServiceAccountBuilder;
use super::MinecraftClusterReconciler;
use crate::reconcilers::builder::ResourceBuilder;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;

pub struct ProxyRoleBindingBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder for ProxyRoleBindingBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = RoleBinding;

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
        let role_binding = RoleBinding {
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
            role_ref: RoleRef {
                api_group: "rbac.authorization.k8s.io".to_string(),
                kind: "Role".to_string(),
                name: ProxyRoleBuilder::name(cluster),
            },
            ..RoleBinding::default()
        };

        Ok(role_binding)
    }

    async fn update(
        &self,
        cluster: &Self::OwnerType,
        role_binding: &mut Self::ResourceType,
    ) -> Result<(), anyhow::Error> {
        role_binding.subjects = Some(vec![Subject {
            kind: "ServiceAccount".to_string(),
            name: ProxyServiceAccountBuilder::name(cluster),
            namespace: cluster.namespace(),
            ..Subject::default()
        }]);

        Ok(())
    }
}

impl ProxyRoleBindingBuilder {
    pub fn new(client: Client) -> Self {
        ProxyRoleBindingBuilder { client }
    }
}

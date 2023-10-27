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
impl<'a> ResourceBuilder<'a> for ProxyRoleBindingBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = RoleBinding;
    type Context = ();

    fn name(cluster: &Self::OwnerType) -> String {
        format!("shulker:{}:proxy", cluster.name_any())
    }

    fn api(&self, cluster: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(self.client.clone(), cluster.namespace().as_ref().unwrap())
    }

    fn is_recreation_needed(cluster: &Self::OwnerType, role_binding: &Self::ResourceType) -> bool {
        role_binding.role_ref.name != ProxyRoleBuilder::name(cluster)
    }

    async fn build(
        &self,
        cluster: &Self::OwnerType,
        name: &str,
        _existing_role_binding: Option<&Self::ResourceType>,
        _context: Option<Self::Context>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let role_binding = RoleBinding {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(cluster.namespace().unwrap().clone()),
                labels: Some(MinecraftClusterReconciler::get_labels(
                    cluster,
                    "role-binding".to_string(),
                    "proxy-rbac".to_string(),
                )),
                ..ObjectMeta::default()
            },
            role_ref: RoleRef {
                api_group: "rbac.authorization.k8s.io".to_string(),
                kind: "Role".to_string(),
                name: ProxyRoleBuilder::name(cluster),
            },
            subjects: Some(vec![Subject {
                kind: "ServiceAccount".to_string(),
                name: ProxyServiceAccountBuilder::name(cluster),
                namespace: cluster.namespace(),
                ..Subject::default()
            }]),
        };

        Ok(role_binding)
    }
}

impl ProxyRoleBindingBuilder {
    pub fn new(client: Client) -> Self {
        ProxyRoleBindingBuilder { client }
    }
}

#[cfg(test)]
mod tests {
    use k8s_openapi::api::rbac::v1::{RoleBinding, RoleRef, Subject};

    use crate::reconcilers::{
        builder::ResourceBuilder,
        minecraft_cluster::fixtures::{create_client_mock, TEST_CLUSTER},
    };

    #[test]
    fn name_contains_cluster_name() {
        // W
        let name = super::ProxyRoleBindingBuilder::name(&TEST_CLUSTER);

        // T
        assert_eq!(name, "shulker:my-cluster:proxy");
    }

    #[test]
    fn is_recreation_needed_if_role_name_changed() {
        // G
        let role_binding = RoleBinding {
            role_ref: RoleRef {
                name: "old-role".to_string(),
                ..RoleRef::default()
            },
            ..RoleBinding::default()
        };

        // W
        let is_recreation_needed =
            super::ProxyRoleBindingBuilder::is_recreation_needed(&TEST_CLUSTER, &role_binding);

        // T
        assert!(is_recreation_needed);
    }

    #[test]
    fn is_recreation_needed_if_same_role_name() {
        // G
        let role_binding = RoleBinding {
            role_ref: RoleRef {
                name: super::ProxyRoleBuilder::name(&TEST_CLUSTER),
                ..RoleRef::default()
            },
            ..RoleBinding::default()
        };

        // W
        let is_recreation_needed =
            super::ProxyRoleBindingBuilder::is_recreation_needed(&TEST_CLUSTER, &role_binding);

        // T
        assert!(!is_recreation_needed);
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::ProxyRoleBindingBuilder::new(client);
        let name = super::ProxyRoleBindingBuilder::name(&TEST_CLUSTER);

        // W
        let role_binding = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(role_binding);
    }

    #[tokio::test]
    async fn build_has_role() {
        // G
        let client = create_client_mock();
        let builder = super::ProxyRoleBindingBuilder::new(client);
        let name = super::ProxyRoleBindingBuilder::name(&TEST_CLUSTER);

        // W
        let role_binding = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        assert_eq!(
            role_binding.role_ref,
            RoleRef {
                api_group: "rbac.authorization.k8s.io".to_string(),
                kind: "Role".to_string(),
                name: "shulker:my-cluster:proxy".to_string(),
            }
        );
    }

    #[tokio::test]
    async fn build_has_subjects() {
        // G
        let client = create_client_mock();
        let builder = super::ProxyRoleBindingBuilder::new(client);
        let name = super::ProxyRoleBindingBuilder::name(&TEST_CLUSTER);

        // W
        let role_binding = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        assert_eq!(
            role_binding.subjects,
            Some(vec![Subject {
                kind: "ServiceAccount".to_string(),
                name: "shulker-my-cluster-proxy".to_string(),
                namespace: Some("default".to_string()),
                ..Subject::default()
            }])
        )
    }
}

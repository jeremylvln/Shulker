use k8s_openapi::api::rbac::v1::PolicyRule;
use k8s_openapi::api::rbac::v1::Role;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use super::MinecraftClusterReconciler;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

pub struct MinecraftServerRoleBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl<'a> ResourceBuilder<'a> for MinecraftServerRoleBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = Role;
    type Context = ();

    fn name(cluster: &Self::OwnerType) -> String {
        format!("shulker:{}:server", cluster.name_any())
    }

    fn api(&self, cluster: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(self.client.clone(), cluster.namespace().as_ref().unwrap())
    }

    async fn build(
        &self,
        cluster: &Self::OwnerType,
        name: &str,
        _existing_role: Option<&Self::ResourceType>,
        _context: Option<Self::Context>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let role = Role {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(cluster.namespace().unwrap().clone()),
                labels: Some(MinecraftClusterReconciler::get_labels(
                    cluster,
                    "role".to_string(),
                    "minecraft-server-rbac".to_string(),
                )),
                ..ObjectMeta::default()
            },
            rules: Some(vec![
                PolicyRule {
                    api_groups: Some(vec!["".to_string()]),
                    resources: Some(vec!["events".to_string()]),
                    verbs: vec!["create".to_string(), "patch".to_string()],
                    ..PolicyRule::default()
                },
                PolicyRule {
                    api_groups: Some(vec!["agones.dev".to_string()]),
                    resources: Some(vec!["gameservers".to_string()]),
                    verbs: vec![
                        "list".to_string(),
                        "watch".to_string(),
                        "update".to_string(),
                    ],
                    ..PolicyRule::default()
                },
            ]),
        };

        Ok(role)
    }
}

impl MinecraftServerRoleBuilder {
    pub fn new(client: Client) -> Self {
        MinecraftServerRoleBuilder { client }
    }
}

#[cfg(test)]
mod tests {
    use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

    use crate::reconcilers::minecraft_cluster::fixtures::{create_client_mock, TEST_CLUSTER};

    #[test]
    fn name_contains_cluster_name() {
        // W
        let name = super::MinecraftServerRoleBuilder::name(&TEST_CLUSTER);

        // T
        assert_eq!(name, "shulker:my-cluster:server");
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::MinecraftServerRoleBuilder::new(client);
        let name = super::MinecraftServerRoleBuilder::name(&TEST_CLUSTER);

        // W
        let role = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(role);
    }

    #[tokio::test]
    async fn build_can_create_events() {
        // G
        let client = create_client_mock();
        let builder = super::MinecraftServerRoleBuilder::new(client);
        let name = super::MinecraftServerRoleBuilder::name(&TEST_CLUSTER);

        // W
        let role = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        assert!(role.rules.as_ref().unwrap().iter().any(|rule| {
            rule.api_groups == Some(vec!["".to_string()])
                && rule.resources == Some(vec!["events".to_string()])
                && rule.verbs.contains(&"create".to_string())
        }));
    }

    #[tokio::test]
    async fn build_can_watch_gameservers() {
        // G
        let client = create_client_mock();
        let builder = super::MinecraftServerRoleBuilder::new(client);
        let name = super::MinecraftServerRoleBuilder::name(&TEST_CLUSTER);

        // W
        let role = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        assert!(role.rules.as_ref().unwrap().iter().any(|rule| {
            rule.api_groups == Some(vec!["agones.dev".to_string()])
                && rule.resources == Some(vec!["gameservers".to_string()])
                && rule.verbs.contains(&"list".to_string())
        }));
        assert!(role.rules.as_ref().unwrap().iter().any(|rule| {
            rule.api_groups == Some(vec!["agones.dev".to_string()])
                && rule.resources == Some(vec!["gameservers".to_string()])
                && rule.verbs.contains(&"watch".to_string())
        }));
    }

    #[tokio::test]
    async fn build_can_update_gameservers() {
        // G
        let client = create_client_mock();
        let builder = super::MinecraftServerRoleBuilder::new(client);
        let name = super::MinecraftServerRoleBuilder::name(&TEST_CLUSTER);

        // W
        let role = builder
            .build(&TEST_CLUSTER, &name, None, None)
            .await
            .unwrap();

        // T
        assert!(role.rules.as_ref().unwrap().iter().any(|rule| {
            rule.api_groups == Some(vec!["agones.dev".to_string()])
                && rule.resources == Some(vec!["gameservers".to_string()])
                && rule.verbs.contains(&"update".to_string())
        }));
    }
}

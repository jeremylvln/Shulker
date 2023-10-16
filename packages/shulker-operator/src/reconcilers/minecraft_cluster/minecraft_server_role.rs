use k8s_openapi::api::rbac::v1::PolicyRule;
use k8s_openapi::api::rbac::v1::Role;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use super::MinecraftClusterReconciler;
use crate::reconcilers::builder::ResourceBuilder;
use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;

pub struct MinecraftServerRoleBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder for MinecraftServerRoleBuilder {
    type OwnerType = MinecraftCluster;
    type ResourceType = Role;

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
        let role = Role {
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
            ..Role::default()
        };

        Ok(role)
    }

    async fn update(
        &self,
        _cluster: &Self::OwnerType,
        role: &mut Self::ResourceType,
    ) -> Result<(), anyhow::Error> {
        role.rules = Some(vec![
            PolicyRule {
                api_groups: Some(vec!["".to_string()]),
                resources: Some(vec!["events".to_string()]),
                verbs: vec!["create".to_string()],
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
        ]);

        Ok(())
    }
}

impl MinecraftServerRoleBuilder {
    pub fn new(client: Client) -> Self {
        MinecraftServerRoleBuilder { client }
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
        let name = super::MinecraftServerRoleBuilder::name(&TEST_CLUSTER);

        // T
        assert_eq!(name, "my-cluster-server");
    }

    #[tokio::test]
    async fn create_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::MinecraftServerRoleBuilder::new(client);

        // W
        let role = builder
            .create(&TEST_CLUSTER, "my-cluster-server")
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(role);
    }

    #[tokio::test]
    async fn update_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::MinecraftServerRoleBuilder::new(client);
        let mut role = builder
            .create(&TEST_CLUSTER, "my-cluster-server")
            .await
            .unwrap();

        // W
        builder.update(&TEST_CLUSTER, &mut role).await.unwrap();

        // T
        insta::assert_yaml_snapshot!(role);
    }

    #[tokio::test]
    async fn update_can_create_events() {
        // G
        let client = create_client_mock();
        let builder = super::MinecraftServerRoleBuilder::new(client);
        let mut role = builder
            .create(&TEST_CLUSTER, "my-cluster-server")
            .await
            .unwrap();

        // W
        builder.update(&TEST_CLUSTER, &mut role).await.unwrap();

        // T
        assert!(role.rules.as_ref().unwrap().iter().any(|rule| {
            rule.api_groups == Some(vec!["".to_string()])
                && rule.resources == Some(vec!["events".to_string()])
                && rule.verbs.contains(&"create".to_string())
        }));
    }

    #[tokio::test]
    async fn update_can_watch_gameservers() {
        // G
        let client = create_client_mock();
        let builder = super::MinecraftServerRoleBuilder::new(client);
        let mut role = builder
            .create(&TEST_CLUSTER, "my-cluster-server")
            .await
            .unwrap();

        // W
        builder.update(&TEST_CLUSTER, &mut role).await.unwrap();

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
    async fn update_can_update_gameservers() {
        // G
        let client = create_client_mock();
        let builder = super::MinecraftServerRoleBuilder::new(client);
        let mut role = builder
            .create(&TEST_CLUSTER, "my-cluster-server")
            .await
            .unwrap();

        // W
        builder.update(&TEST_CLUSTER, &mut role).await.unwrap();

        // T
        assert!(role.rules.as_ref().unwrap().iter().any(|rule| {
            rule.api_groups == Some(vec!["agones.dev".to_string()])
                && rule.resources == Some(vec!["gameservers".to_string()])
                && rule.verbs.contains(&"update".to_string())
        }));
    }
}

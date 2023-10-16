use k8s_openapi::apimachinery::pkg::apis::meta::v1::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use crate::reconcilers::builder::ResourceBuilder;
use google_agones_crds::v1::fleet_autoscaler::FleetAutoscaler;
use google_agones_crds::v1::fleet_autoscaler::FleetAutoscalerSpec;
use shulker_crds::v1alpha1::proxy_fleet::ProxyFleet;

use super::fleet::FleetBuilder;
use super::ProxyFleetReconciler;

pub struct FleetAutoscalerBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder for FleetAutoscalerBuilder {
    type OwnerType = ProxyFleet;
    type ResourceType = FleetAutoscaler;

    fn name(proxy_fleet: &Self::OwnerType) -> String {
        proxy_fleet.name_any()
    }

    fn is_updatable() -> bool {
        true
    }

    fn api(&self, proxy_fleet: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(
            self.client.clone(),
            proxy_fleet.namespace().as_ref().unwrap(),
        )
    }

    fn is_needed(&self, proxy_fleet: &Self::OwnerType) -> bool {
        match &proxy_fleet.spec.autoscaling {
            Some(autoscaling) => autoscaling.agones_policy.is_some(),
            None => false,
        }
    }

    async fn create(
        &self,
        proxy_fleet: &Self::OwnerType,
        name: &str,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let fleet_autoscaler = FleetAutoscaler {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(proxy_fleet.namespace().unwrap().clone()),
                labels: Some(
                    ProxyFleetReconciler::get_common_labels(proxy_fleet)
                        .into_iter()
                        .collect(),
                ),
                ..ObjectMeta::default()
            },
            spec: FleetAutoscalerSpec::default(),
            status: None,
        };

        Ok(fleet_autoscaler)
    }

    async fn update(
        &self,
        proxy_fleet: &Self::OwnerType,
        fleet_autoscaler: &mut Self::ResourceType,
    ) -> Result<(), anyhow::Error> {
        fleet_autoscaler.spec = FleetAutoscalerSpec {
            fleet_name: FleetBuilder::name(proxy_fleet),
            policy: proxy_fleet
                .spec
                .autoscaling
                .as_ref()
                .unwrap()
                .agones_policy
                .as_ref()
                .unwrap()
                .clone(),
        };
        Ok(())
    }
}

impl FleetAutoscalerBuilder {
    pub fn new(client: Client) -> Self {
        FleetAutoscalerBuilder { client }
    }
}

#[cfg(test)]
mod tests {
    use crate::reconcilers::{
        builder::ResourceBuilder,
        proxy_fleet::fixtures::{create_client_mock, TEST_PROXY_FLEET},
    };

    #[test]
    fn name_contains_fleet_name() {
        // W
        let name = super::FleetAutoscalerBuilder::name(&TEST_PROXY_FLEET);

        // T
        assert_eq!(name, "my-proxy");
    }

    #[tokio::test]
    async fn is_needed_with_autoscaling() {
        // G
        let client = create_client_mock();
        let builder = super::FleetAutoscalerBuilder::new(client);

        // W
        let is_needed = builder.is_needed(&TEST_PROXY_FLEET);

        // T
        assert!(is_needed);
    }

    #[tokio::test]
    async fn is_needed_without_autoscaling() {
        // G
        let client = create_client_mock();
        let builder = super::FleetAutoscalerBuilder::new(client);
        let mut fleet = TEST_PROXY_FLEET.clone();
        fleet.spec.autoscaling = None;

        // W
        let is_needed = builder.is_needed(&fleet);

        // T
        assert!(!is_needed);
    }

    #[tokio::test]
    async fn create_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::FleetAutoscalerBuilder::new(client);

        // W
        let fleet_autoscaler = builder.create(&TEST_PROXY_FLEET, "my-proxy").await.unwrap();

        // T
        insta::assert_yaml_snapshot!(fleet_autoscaler);
    }

    #[tokio::test]
    async fn update_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::FleetAutoscalerBuilder::new(client);
        let mut fleet_autoscaler = builder.create(&TEST_PROXY_FLEET, "my-proxy").await.unwrap();

        // W
        builder
            .update(&TEST_PROXY_FLEET, &mut fleet_autoscaler)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(fleet_autoscaler);
    }
}

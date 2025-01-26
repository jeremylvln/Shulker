use k8s_openapi::apimachinery::pkg::apis::meta::v1::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use google_agones_crds::v1::fleet_autoscaler::FleetAutoscaler;
use google_agones_crds::v1::fleet_autoscaler::FleetAutoscalerSpec;
use shulker_crds::v1alpha1::proxy_fleet::ProxyFleet;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

use super::fleet::FleetBuilder;
use super::ProxyFleetReconciler;

pub struct FleetAutoscalerBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder<'_> for FleetAutoscalerBuilder {
    type OwnerType = ProxyFleet;
    type ResourceType = FleetAutoscaler;
    type Context = ();

    fn name(proxy_fleet: &Self::OwnerType) -> String {
        proxy_fleet.name_any()
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

    async fn build(
        &self,
        proxy_fleet: &Self::OwnerType,
        name: &str,
        _existing_fleet_autoscaler: Option<&Self::ResourceType>,
        _context: Option<Self::Context>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let fleet_autoscaler = FleetAutoscaler {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(proxy_fleet.namespace().unwrap().clone()),
                labels: Some(ProxyFleetReconciler::get_labels(
                    proxy_fleet,
                    "fleet-autoscaler".to_string(),
                    "proxy".to_string(),
                )),
                ..ObjectMeta::default()
            },
            spec: FleetAutoscalerSpec {
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
            },
            status: None,
        };

        Ok(fleet_autoscaler)
    }
}

impl FleetAutoscalerBuilder {
    pub fn new(client: Client) -> Self {
        FleetAutoscalerBuilder { client }
    }
}

#[cfg(test)]
mod tests {
    use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

    use crate::reconcilers::proxy_fleet::fixtures::{create_client_mock, TEST_PROXY_FLEET};

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
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::FleetAutoscalerBuilder::new(client);
        let name = super::FleetAutoscalerBuilder::name(&TEST_PROXY_FLEET);

        // W
        let fleet_autoscaler = builder
            .build(&TEST_PROXY_FLEET, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(fleet_autoscaler);
    }
}

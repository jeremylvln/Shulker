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

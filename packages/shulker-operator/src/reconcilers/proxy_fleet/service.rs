use k8s_openapi::api::core::v1::Service;
use k8s_openapi::api::core::v1::ServicePort;
use k8s_openapi::api::core::v1::ServiceSpec;
use k8s_openapi::apimachinery::pkg::util::intstr::IntOrString;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use crate::reconcilers::builder::ResourceBuilder;
use shulker_crds::v1alpha1::proxy_fleet::ProxyFleet;

use super::ProxyFleetReconciler;

pub struct ServiceBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder for ServiceBuilder {
    type OwnerType = ProxyFleet;
    type ResourceType = Service;

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
        proxy_fleet.spec.service.is_some()
    }

    async fn build(
        &self,
        proxy_fleet: &Self::OwnerType,
        name: &str,
        _existing_service: Option<&Self::ResourceType>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let service_config = proxy_fleet.spec.service.as_ref().unwrap();
        let service = Service {
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
            spec: Some(ServiceSpec {
                selector: Some(
                    ProxyFleetReconciler::get_common_labels(proxy_fleet)
                        .into_iter()
                        .collect(),
                ),
                type_: Some(service_config.type_.to_string()),
                external_traffic_policy: service_config
                    .external_traffic_policy
                    .as_ref()
                    .map(|x| x.to_string()),
                ports: Some(vec![ServicePort {
                    name: Some("minecraft".to_string()),
                    protocol: Some("TCP".to_string()),
                    port: 25565,
                    target_port: Some(IntOrString::Int(25577)),
                    ..ServicePort::default()
                }]),
                ..ServiceSpec::default()
            }),
            ..Service::default()
        };

        Ok(service)
    }
}

impl ServiceBuilder {
    pub fn new(client: Client) -> Self {
        ServiceBuilder { client }
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
        let name = super::ServiceBuilder::name(&TEST_PROXY_FLEET);

        // T
        assert_eq!(name, "my-proxy");
    }

    #[tokio::test]
    async fn is_needed_with_service() {
        // G
        let client = create_client_mock();
        let builder = super::ServiceBuilder::new(client);

        // W
        let is_needed = builder.is_needed(&TEST_PROXY_FLEET);

        // T
        assert!(is_needed);
    }

    #[tokio::test]
    async fn is_needed_without_service() {
        // G
        let client = create_client_mock();
        let builder = super::ServiceBuilder::new(client);
        let mut fleet = TEST_PROXY_FLEET.clone();
        fleet.spec.service = None;

        // W
        let is_needed = builder.is_needed(&fleet);

        // T
        assert!(!is_needed);
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::ServiceBuilder::new(client);

        // W
        let service = builder
            .build(&TEST_PROXY_FLEET, "my-proxy", None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(service);
    }
}

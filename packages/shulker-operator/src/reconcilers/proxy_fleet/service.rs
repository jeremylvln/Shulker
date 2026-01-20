use k8s_openapi::api::core::v1::Service;
use k8s_openapi::api::core::v1::ServicePort;
use k8s_openapi::api::core::v1::ServiceSpec;
use k8s_openapi::apimachinery::pkg::util::intstr::IntOrString;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use shulker_crds::v1alpha1::proxy_fleet::ProxyFleet;
use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

use super::ProxyFleetReconciler;

pub struct ServiceBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder<'_> for ServiceBuilder {
    type OwnerType = ProxyFleet;
    type ResourceType = Service;
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
        proxy_fleet.spec.service.is_some()
    }

    async fn build(
        &self,
        proxy_fleet: &Self::OwnerType,
        name: &str,
        _existing_service: Option<&Self::ResourceType>,
        _context: Option<Self::Context>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let service_config = proxy_fleet.spec.service.as_ref().unwrap();
        let service = Service {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(proxy_fleet.namespace().unwrap().clone()),
                labels: Some({
                    let mut labels = ProxyFleetReconciler::get_labels(
                        proxy_fleet,
                        "proxy".to_string(),
                        "proxy".to_string(),
                    );
                    if let Some(custom_labels) = &service_config.labels {
                        labels.extend(custom_labels.clone());
                    }
                    labels
                }),
                annotations: service_config.annotations.clone(),
                ..ObjectMeta::default()
            },
            spec: Some(ServiceSpec {
                selector: Some(ProxyFleetReconciler::get_labels(
                    proxy_fleet,
                    "proxy".to_string(),
                    "proxy".to_string(),
                )),
                type_: Some(service_config.type_.to_string()),
                external_traffic_policy: service_config
                    .external_traffic_policy
                    .as_ref()
                    .map(|x| x.to_string()),
                ports: Some(vec![ServicePort {
                    name: Some("minecraft".to_string()),
                    protocol: Some("TCP".to_string()),
                    port: 25565,
                    target_port: Some(IntOrString::String("minecraft".to_string())),
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
    use std::collections::BTreeMap;

    use shulker_kube_utils::reconcilers::builder::ResourceBuilder;

    use crate::reconcilers::proxy_fleet::fixtures::{create_client_mock, TEST_PROXY_FLEET};

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
        let name = super::ServiceBuilder::name(&TEST_PROXY_FLEET);

        // W
        let service = builder
            .build(&TEST_PROXY_FLEET, &name, None, None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(service);
    }

    #[tokio::test]
    async fn build_add_custom_annotations() {
        // G
        let client = create_client_mock();
        let builder = super::ServiceBuilder::new(client);
        let custom_annotations = BTreeMap::from([(
            "service.beta.kubernetes.io/load-balancer".to_string(),
            "internal".to_string(),
        )]);
        let mut proxy_fleet = TEST_PROXY_FLEET.clone();
        proxy_fleet.spec.service.as_mut().unwrap().annotations = Some(custom_annotations.clone());
        let name = super::ServiceBuilder::name(&proxy_fleet);

        // W
        let service = builder
            .build(&proxy_fleet, &name, None, None)
            .await
            .unwrap();

        // T
        assert_eq!(service.metadata.annotations.unwrap(), custom_annotations);
    }
    #[tokio::test]
    async fn build_add_custom_labels() {
        // G
        let client = create_client_mock();
        let builder = super::ServiceBuilder::new(client);
        let custom_labels = BTreeMap::from([(
            "my-custom-label".to_string(),
            "my-value".to_string(),
        )]);
        let mut proxy_fleet = TEST_PROXY_FLEET.clone();
        proxy_fleet.spec.service.as_mut().unwrap().labels = Some(custom_labels.clone());
        let name = super::ServiceBuilder::name(&proxy_fleet);

        // W
        let service = builder
            .build(&proxy_fleet, &name, None, None)
            .await
            .unwrap();

        // T
        let service_labels = service.metadata.labels.unwrap();
        assert_eq!(service_labels.get("my-custom-label").unwrap(), "my-value");
        // Ensure default labels are still present
        assert!(service_labels.contains_key("app.kubernetes.io/name"));
    }
}


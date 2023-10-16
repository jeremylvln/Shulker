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
        proxy_fleet.spec.service.is_some()
    }

    async fn create(
        &self,
        proxy_fleet: &Self::OwnerType,
        name: &str,
    ) -> Result<Self::ResourceType, anyhow::Error> {
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
            ..Service::default()
        };

        Ok(service)
    }

    async fn update(
        &self,
        proxy_fleet: &Self::OwnerType,
        service: &mut Self::ResourceType,
    ) -> Result<(), anyhow::Error> {
        let service_config = proxy_fleet.spec.service.as_ref().unwrap();

        service.spec = Some(ServiceSpec {
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
        });

        Ok(())
    }
}

impl ServiceBuilder {
    pub fn new(client: Client) -> Self {
        ServiceBuilder { client }
    }
}

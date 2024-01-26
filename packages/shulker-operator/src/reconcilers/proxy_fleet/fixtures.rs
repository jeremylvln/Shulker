use std::collections::BTreeMap;

use google_agones_crds::v1::fleet_autoscaler::{
    FleetAutoscalerPolicy, FleetAutoscalerPolicyBufferSpec, FleetAutoscalerPolicySpec,
};
use http::{Request, Response};
use k8s_openapi::{api::core::v1::EnvVar, apimachinery::pkg::util::intstr::IntOrString};
use kube::client::Body;
use kube::{core::ObjectMeta, Client};
use lazy_static::lazy_static;
use shulker_crds::{
    resourceref::ResourceRefSpec,
    schemas::{FleetAutoscalingSpec, TemplateSpec},
    v1alpha1::{
        minecraft_cluster::MinecraftClusterRef,
        proxy_fleet::{
            ProxyFleet, ProxyFleetServiceExternalTrafficPolicy, ProxyFleetServiceSpec,
            ProxyFleetServiceType, ProxyFleetSpec, ProxyFleetTemplateConfigurationSpec,
            ProxyFleetTemplatePodOverridesSpec, ProxyFleetTemplateSpec, ProxyFleetTemplateVersion,
            ProxyFleetTemplateVersionSpec,
        },
    },
};

lazy_static! {
    pub static ref TEST_PROXY_FLEET: ProxyFleet = ProxyFleet {
        metadata: ObjectMeta {
            namespace: Some("default".to_string()),
            name: Some("my-proxy".to_string()),
            ..ObjectMeta::default()
        },
        spec: ProxyFleetSpec {
            cluster_ref: MinecraftClusterRef::new("my-cluster".to_string()),
            replicas: 3,
            template: TemplateSpec {
                metadata: Some(ObjectMeta {
                    labels: Some(BTreeMap::from([(
                        "test-label/shulkermc.io".to_string(),
                        "my-value".to_string()
                    )])),
                    annotations: Some(BTreeMap::from([(
                        "test-annotation/shulkermc.io".to_string(),
                        "my-value".to_string()
                    )])),
                    ..ObjectMeta::default()
                }),
                spec: ProxyFleetTemplateSpec {
                    version: ProxyFleetTemplateVersionSpec {
                        channel: ProxyFleetTemplateVersion::Velocity,
                        name: "latest".to_string(),
                    },
                    config: ProxyFleetTemplateConfigurationSpec {
                        existing_config_map_name: None,
                        plugins: Some(vec![ResourceRefSpec {
                            url: Some("https://example.com/my_plugin.jar".to_string()),
                            ..ResourceRefSpec::default()
                        }]),
                        patches: Some(vec![ResourceRefSpec {
                            url: Some("https://example.com/my_patch.tar.gz".to_string()),
                            ..ResourceRefSpec::default()
                        }]),
                        max_players: 1000,
                        motd: "A Motd".to_string(),
                        server_icon: "abc==".to_string(),
                        proxy_protocol: true,
                        ttl_seconds: 3600,
                        players_delta_before_exclusion: 15,
                        skip_agent_download: false
                    },
                    pod_overrides: Some(ProxyFleetTemplatePodOverridesSpec {
                        image: None,
                        env: Some(vec![EnvVar {
                            name: "EXTRA_ENV".to_string(),
                            value: Some("my_value".to_string()),
                            ..EnvVar::default()
                        }]),
                        resources: None,
                        affinity: None,
                        node_selector: Some(BTreeMap::from([(
                            "beta.kubernetes.io/os".to_string(),
                            "linux".to_string()
                        )])),
                        tolerations: None,
                        service_account_name: None,
                        volume_mounts: None,
                        volumes: None,
                        ports: None,
                    })
                },
            },
            service: Some(ProxyFleetServiceSpec {
                type_: ProxyFleetServiceType::LoadBalancer,
                annotations: Some(BTreeMap::from([(
                    "service.beta.kubernetes.io/load-balancer".to_string(),
                    "internal".to_string()
                )])),
                external_traffic_policy: Some(ProxyFleetServiceExternalTrafficPolicy::Cluster),
                preferred_reconnection_address: Some("127.0.0.1".to_string())
            }),
            autoscaling: Some(FleetAutoscalingSpec {
                agones_policy: Some(FleetAutoscalerPolicySpec {
                    type_: FleetAutoscalerPolicy::Buffer,
                    buffer: Some(FleetAutoscalerPolicyBufferSpec {
                        max_replicas: 8,
                        min_replicas: 3,
                        buffer_size: IntOrString::Int(2),
                    }),
                    webhook: None,
                })
            }),
        },
        status: None,
    };
}

pub fn create_client_mock() -> Client {
    let (mock_service, _) = tower_test::mock::pair::<Request<Body>, Response<Body>>();
    Client::new(mock_service, "default")
}

use std::collections::BTreeMap;

use http::{Request, Response};
use k8s_openapi::api::core::v1::EnvVar;
use kube::client::Body;
use kube::{core::ObjectMeta, Client};
use lazy_static::lazy_static;
use shulker_crds::{
    resourceref::ResourceRefSpec,
    v1alpha1::{
        minecraft_cluster::MinecraftClusterRef,
        minecraft_server::{
            MinecraftServer, MinecraftServerConfigurationLifecycleStrategy,
            MinecraftServerConfigurationProxyForwardingMode, MinecraftServerConfigurationSpec,
            MinecraftServerPodOverridesSpec, MinecraftServerSpec, MinecraftServerVersion,
            MinecraftServerVersionSpec,
        },
    },
};

lazy_static! {
    pub static ref TEST_SERVER: MinecraftServer = MinecraftServer {
        metadata: ObjectMeta {
            namespace: Some("default".to_string()),
            name: Some("my-server".to_string()),
            ..ObjectMeta::default()
        },
        spec: MinecraftServerSpec {
            cluster_ref: MinecraftClusterRef::new("my-cluster".to_string()),
            tags: vec!["lobby".to_string()],
            version: MinecraftServerVersionSpec {
                channel: MinecraftServerVersion::Paper,
                name: "1.20.1".to_string(),
                ..MinecraftServerVersionSpec::default()
            },
            config: MinecraftServerConfigurationSpec {
                existing_config_map_name: None,
                world: Some(ResourceRefSpec {
                    url: Some("https://example.com/my_world.tar.gz".to_string()),
                    ..ResourceRefSpec::default()
                }),
                plugins: Some(vec![ResourceRefSpec {
                    url: Some("https://example.com/my_plugin.jar".to_string()),
                    ..ResourceRefSpec::default()
                }]),
                patches: Some(vec![ResourceRefSpec {
                    url: Some("https://example.com/my_patch.tar.gz".to_string()),
                    ..ResourceRefSpec::default()
                }]),
                max_players: 42,
                disable_nether: false,
                disable_end: true,
                server_properties: None,
                proxy_forwarding_mode: MinecraftServerConfigurationProxyForwardingMode::Velocity,
                lifecycle_strategy:
                    MinecraftServerConfigurationLifecycleStrategy::AllocateWhenNotEmpty,
                skip_agent_download: false
            },
            pod_overrides: Some(MinecraftServerPodOverridesSpec {
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
        status: None,
    };
}

pub fn create_client_mock() -> Client {
    let (mock_service, _) = tower_test::mock::pair::<Request<Body>, Response<Body>>();
    Client::new(mock_service, "default")
}

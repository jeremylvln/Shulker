use std::collections::BTreeMap;

use k8s_openapi::api::core::v1::ConfigMap;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;
use shulker_crds::v1alpha1::proxy_fleet::ProxyFleetTemplateVersion;

use crate::reconcilers::builder::ResourceBuilder;
use shulker_crds::v1alpha1::proxy_fleet::ProxyFleet;

use super::ProxyFleetReconciler;

pub struct ConfigMapBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder for ConfigMapBuilder {
    type OwnerType = ProxyFleet;
    type ResourceType = ConfigMap;

    fn name(proxy_fleet: &Self::OwnerType) -> String {
        format!("{}-config", proxy_fleet.name_any())
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

    fn is_needed(&self, _proxy_fleet: &Self::OwnerType) -> bool {
        true
    }

    async fn create(
        &self,
        proxy_fleet: &Self::OwnerType,
        name: &str,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let config_map = ConfigMap {
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
            ..ConfigMap::default()
        };

        Ok(config_map)
    }

    async fn update(
        &self,
        proxy_fleet: &Self::OwnerType,
        config_map: &mut Self::ResourceType,
    ) -> Result<(), anyhow::Error> {
        let mut data = BTreeMap::from([
            (
                "init-fs.sh".to_string(),
                include_str!("../../../assets/proxy-init-fs.sh").to_string(),
            ),
            (
                "probe-readiness.sh".to_string(),
                include_str!("../../../assets/proxy-probe-readiness.sh").to_string(),
            ),
            (
                "server-icon.png".to_string(),
                proxy_fleet.spec.template.spec.config.server_icon.clone(),
            ),
        ]);

        match proxy_fleet.spec.template.spec.version.channel {
            ProxyFleetTemplateVersion::Velocity => {
                data.insert(
                    "velocity-config.toml".to_string(),
                    velocity::VelocityToml::from_spec(&proxy_fleet.spec.template.spec.config)
                        .to_string(),
                );
            }
            _ => {
                data.insert(
                    "bungeecord-config.yml".to_string(),
                    bungeecord::BungeeCordYml::from_spec(&proxy_fleet.spec.template.spec.config)
                        .to_string(),
                );
            }
        }

        config_map.data = Some(data);

        Ok(())
    }
}

impl ConfigMapBuilder {
    pub fn new(client: Client) -> Self {
        ConfigMapBuilder { client }
    }
}

mod bungeecord {
    use serde::{Deserialize, Serialize};
    use std::collections::HashMap;

    use shulker_crds::v1alpha1::proxy_fleet::ProxyFleetTemplateConfigurationSpec;

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "snake_case")]
    pub struct BungeeCordYml {
        servers: HashMap<String, BungeeCordServerYml>,
        listeners: Vec<BungeeCordListenerYml>,
        groups: HashMap<String, ()>,
        online_mode: bool,
        ip_forward: bool,
        prevent_proxy_connections: bool,
        enforce_secure_profile: bool,
        log_pings: bool,
    }

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "snake_case")]
    pub struct BungeeCordServerYml {
        motd: String,
        address: String,
        restricted: bool,
    }

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "snake_case")]
    pub struct BungeeCordListenerYml {
        host: String,
        query_port: u16,
        motd: String,
        max_players: u32,
        priorities: Vec<String>,
        ping_passthrough: bool,
        force_default_server: bool,
        proxy_protocol: bool,
    }

    impl BungeeCordYml {
        pub fn from_spec(spec: &ProxyFleetTemplateConfigurationSpec) -> Self {
            BungeeCordYml {
                servers: HashMap::from([
                    (
                        "lobby".to_string(),
                        BungeeCordServerYml {
                            motd: spec.motd.clone(),
                            address: "localhost:30000".to_string(),
                            restricted: false,
                        },
                    ),
                    (
                        "limbo".to_string(),
                        BungeeCordServerYml {
                            motd: spec.motd.clone(),
                            address: "localhost:30001".to_string(),
                            restricted: false,
                        },
                    ),
                ]),
                listeners: vec![BungeeCordListenerYml {
                    host: "0.0.0.0:25577".to_string(),
                    query_port: 25577,
                    motd: spec.motd.clone(),
                    max_players: spec.max_players,
                    priorities: vec!["lobby".to_string(), "limbo".to_string()],
                    ping_passthrough: false,
                    force_default_server: true,
                    proxy_protocol: spec.proxy_protocol,
                }],
                groups: HashMap::new(),
                online_mode: true,
                ip_forward: true,
                prevent_proxy_connections: true,
                enforce_secure_profile: true,
                log_pings: false,
            }
        }
    }

    impl ToString for BungeeCordYml {
        fn to_string(&self) -> String {
            let mut yml = serde_yaml::to_string(&self).unwrap();
            yml.push('\n');

            yml
        }
    }
}

mod velocity {
    use serde::{Deserialize, Serialize};

    use shulker_crds::v1alpha1::proxy_fleet::ProxyFleetTemplateConfigurationSpec;

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct VelocityToml {
        config_version: String,
        bind: String,
        motd: String,
        show_max_players: u32,
        online_mode: bool,
        force_key_authentication: bool,
        prevent_client_proxy_connections: bool,
        forwarding_secret_file: String,
        player_info_forwarding_mode: String,
        servers: VelocityServersToml,
        forced_hosts: VelocityForcedHostsToml,
        advanced: VelocityAdvancedToml,
    }

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct VelocityServersToml {
        lobby: String,
        limbo: String,
        #[serde(rename = "try")]
        try_: Vec<String>,
    }

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct VelocityForcedHostsToml {}

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct VelocityAdvancedToml {
        haproxy_protocol: bool,
        tcp_fast_open: bool,
    }

    impl VelocityToml {
        pub fn from_spec(spec: &ProxyFleetTemplateConfigurationSpec) -> Self {
            VelocityToml {
                config_version: "2.5".to_string(),
                bind: "0.0.0.0:25577".to_string(),
                motd: spec.motd.clone(),
                show_max_players: spec.max_players,
                online_mode: true,
                force_key_authentication: true,
                prevent_client_proxy_connections: true,
                player_info_forwarding_mode: "modern".to_string(),
                forwarding_secret_file: "/mnt/shulker/forwarding-secret/key".to_string(),
                servers: VelocityServersToml {
                    lobby: "localhost:30000".to_string(),
                    limbo: "localhost:30001".to_string(),
                    try_: vec!["lobby".to_string(), "limbo".to_string()],
                },
                forced_hosts: VelocityForcedHostsToml {},
                advanced: VelocityAdvancedToml {
                    haproxy_protocol: spec.proxy_protocol,
                    tcp_fast_open: true,
                },
            }
        }
    }

    impl ToString for VelocityToml {
        fn to_string(&self) -> String {
            let mut toml = toml::to_string(&self).unwrap();
            toml.push('\n');

            toml
        }
    }
}

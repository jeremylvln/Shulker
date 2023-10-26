use std::collections::BTreeMap;

use k8s_openapi::api::core::v1::ConfigMap;
use kube::core::ObjectMeta;
use kube::Api;
use kube::Client;
use kube::ResourceExt;

use crate::reconcilers::builder::ResourceBuilder;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServer;
use shulker_crds::v1alpha1::minecraft_server::MinecraftServerConfigurationSpec;

use super::MinecraftServerReconciler;

pub struct ConfigMapBuilder {
    client: Client,
}

#[async_trait::async_trait]
impl ResourceBuilder for ConfigMapBuilder {
    type OwnerType = MinecraftServer;
    type ResourceType = ConfigMap;

    fn name(minecraft_server: &Self::OwnerType) -> String {
        format!("{}-config", minecraft_server.name_any())
    }

    fn api(&self, minecraft_server: &Self::OwnerType) -> kube::Api<Self::ResourceType> {
        Api::namespaced(
            self.client.clone(),
            minecraft_server.namespace().as_ref().unwrap(),
        )
    }

    fn is_needed(&self, minecraft_server: &Self::OwnerType) -> bool {
        minecraft_server
            .spec
            .config
            .existing_config_map_name
            .is_none()
    }

    async fn build(
        &self,
        minecraft_server: &Self::OwnerType,
        name: &str,
        _existing_config_map: Option<&Self::ResourceType>,
    ) -> Result<Self::ResourceType, anyhow::Error> {
        let config_map = ConfigMap {
            metadata: ObjectMeta {
                name: Some(name.to_string()),
                namespace: Some(minecraft_server.namespace().unwrap().clone()),
                labels: Some(
                    MinecraftServerReconciler::get_common_labels(minecraft_server)
                        .into_iter()
                        .collect(),
                ),
                ..ObjectMeta::default()
            },
            data: Some(Self::get_data_from_spec(&minecraft_server.spec.config)),
            ..ConfigMap::default()
        };

        Ok(config_map)
    }
}

impl ConfigMapBuilder {
    pub fn new(client: Client) -> Self {
        ConfigMapBuilder { client }
    }

    pub fn get_data_from_spec(spec: &MinecraftServerConfigurationSpec) -> BTreeMap<String, String> {
        BTreeMap::from([
            (
                "init-fs.sh".to_string(),
                include_str!("../../../assets/server-init-fs.sh").to_string(),
            ),
            (
                "server.properties".to_string(),
                vanilla::VanillaProperties::from_spec(spec).to_string(),
            ),
            (
                "bukkit-config.yml".to_string(),
                bukkit::BukkitYml::from_spec(spec).to_string(),
            ),
            (
                "spigot-config.yml".to_string(),
                spigot::SpigotYml::from_spec(spec).to_string(),
            ),
            (
                "paper-global-config.yml".to_string(),
                paper::PaperGlobalYml::from_spec(spec).to_string(),
            ),
        ])
    }
}

#[cfg(test)]
mod tests {
    use crate::reconcilers::{
        builder::ResourceBuilder,
        minecraft_server::fixtures::{create_client_mock, TEST_SERVER},
    };

    #[test]
    fn name_contains_server_name() {
        // W
        let name = super::ConfigMapBuilder::name(&TEST_SERVER);

        // T
        assert_eq!(name, "my-server-config");
    }

    #[tokio::test]
    async fn build_snapshot() {
        // G
        let client = create_client_mock();
        let builder = super::ConfigMapBuilder::new(client);

        // W
        let config_map = builder
            .build(&TEST_SERVER, "my-server-config", None)
            .await
            .unwrap();

        // T
        insta::assert_yaml_snapshot!(config_map);
    }

    #[test]
    fn get_data_from_spec_has_startup_script() {
        // G
        let spec = TEST_SERVER.spec.config.clone();

        // W
        let data = super::ConfigMapBuilder::get_data_from_spec(&spec);

        // T
        assert!(data.contains_key("init-fs.sh"));
    }

    #[test]
    fn get_data_from_spec_has_server_configs() {
        // G
        let spec = TEST_SERVER.spec.config.clone();

        // W
        let data = super::ConfigMapBuilder::get_data_from_spec(&spec);

        // T
        assert!(data.contains_key("server.properties"));
        assert!(data.contains_key("bukkit-config.yml"));
        assert!(data.contains_key("spigot-config.yml"));
        assert!(data.contains_key("paper-global-config.yml"));
    }
}

mod vanilla {
    use std::collections::BTreeMap;

    use shulker_crds::v1alpha1::minecraft_server::MinecraftServerConfigurationSpec;

    pub struct VanillaProperties(BTreeMap<String, String>);

    impl VanillaProperties {
        pub fn from_spec(spec: &MinecraftServerConfigurationSpec) -> Self {
            let mut properties = BTreeMap::new();

            properties.insert("max-players".to_string(), spec.max_players.to_string());
            properties.insert(
                "allow-nether".to_string(),
                (!spec.disable_nether).to_string(),
            );
            properties.insert("online-mode".to_string(), "false".to_string());
            properties.insert("prevent-proxy-connections".to_string(), "false".to_string());
            properties.insert("enforce-secure-profiles".to_string(), "true".to_string());

            VanillaProperties(properties)
        }
    }

    impl ToString for VanillaProperties {
        fn to_string(&self) -> String {
            let mut properties = String::new();

            for (key, value) in &self.0 {
                properties.push_str(&format!("{}={}\n", key, value));
            }

            properties
        }
    }

    #[cfg(test)]
    mod tests {
        use std::collections::BTreeMap;

        use crate::reconcilers::minecraft_server::fixtures::TEST_SERVER;

        #[test]
        fn from_spec() {
            // G
            let spec = TEST_SERVER.spec.config.clone();

            // W
            let config = super::VanillaProperties::from_spec(&spec);

            // T
            insta::assert_debug_snapshot!(config.0);
        }

        #[test]
        fn to_string() {
            // G
            let config = super::VanillaProperties(BTreeMap::from([(
                "my-str".to_string(),
                "my-value".to_string(),
            )]));

            // W
            let properties = config.to_string();

            // T
            insta::assert_snapshot!(properties);
        }
    }
}

mod bukkit {
    use serde::{Deserialize, Serialize};

    use shulker_crds::v1alpha1::minecraft_server::MinecraftServerConfigurationSpec;

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct BukkitYml {
        settings: BukkitSettingsYml,
        auto_updater: BukkitAutoUpdaterYml,
    }

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct BukkitSettingsYml {
        allow_end: bool,
    }

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct BukkitAutoUpdaterYml {
        enabled: bool,
    }

    impl BukkitYml {
        pub fn from_spec(spec: &MinecraftServerConfigurationSpec) -> Self {
            BukkitYml {
                settings: BukkitSettingsYml {
                    allow_end: !spec.disable_end,
                },
                auto_updater: BukkitAutoUpdaterYml { enabled: false },
            }
        }
    }

    impl ToString for BukkitYml {
        fn to_string(&self) -> String {
            let mut yml = serde_yaml::to_string(&self).unwrap();
            yml.push('\n');

            yml
        }
    }

    #[cfg(test)]
    mod tests {
        use crate::reconcilers::minecraft_server::fixtures::TEST_SERVER;

        #[test]
        fn from_spec() {
            // G
            let spec = TEST_SERVER.spec.config.clone();

            // W
            let config = super::BukkitYml::from_spec(&spec);

            // T
            insta::assert_yaml_snapshot!(config);
        }

        #[test]
        fn to_string() {
            // G
            let config = super::BukkitYml {
                settings: super::BukkitSettingsYml { allow_end: true },
                auto_updater: super::BukkitAutoUpdaterYml { enabled: false },
            };

            // W
            let yml = config.to_string();

            // T
            insta::assert_snapshot!(yml);
        }
    }
}

mod spigot {
    use serde::{Deserialize, Serialize};

    use shulker_crds::v1alpha1::minecraft_server::{
        MinecraftServerConfigurationProxyForwardingMode, MinecraftServerConfigurationSpec,
    };

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct SpigotYml {
        settings: SpigotSettingsYml,
        advancements: SpigotSaveableYml,
        players: SpigotSaveableYml,
        stats: SpigotSaveableYml,
        save_user_cache_on_stop_only: bool,
    }

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct SpigotSettingsYml {
        bungeecord: bool,
        restart_on_crash: bool,
    }

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct SpigotSaveableYml {
        disable_saving: bool,
    }

    impl SpigotYml {
        pub fn from_spec(spec: &MinecraftServerConfigurationSpec) -> Self {
            SpigotYml {
                settings: SpigotSettingsYml {
                    bungeecord: spec.proxy_forwarding_mode
                        == MinecraftServerConfigurationProxyForwardingMode::BungeeCord,
                    restart_on_crash: false,
                },
                advancements: SpigotSaveableYml {
                    disable_saving: true,
                },
                players: SpigotSaveableYml {
                    disable_saving: true,
                },
                stats: SpigotSaveableYml {
                    disable_saving: true,
                },
                save_user_cache_on_stop_only: true,
            }
        }
    }

    impl ToString for SpigotYml {
        fn to_string(&self) -> String {
            let mut yml = serde_yaml::to_string(&self).unwrap();
            yml.push('\n');

            yml
        }
    }

    #[cfg(test)]
    mod tests {
        use crate::reconcilers::minecraft_server::fixtures::TEST_SERVER;

        #[test]
        fn from_spec() {
            // G
            let spec = TEST_SERVER.spec.config.clone();

            // W
            let config = super::SpigotYml::from_spec(&spec);

            // T
            insta::assert_yaml_snapshot!(config);
        }

        #[test]
        fn to_string() {
            // G
            let config = super::SpigotYml {
                settings: super::SpigotSettingsYml {
                    bungeecord: false,
                    restart_on_crash: false,
                },
                advancements: super::SpigotSaveableYml {
                    disable_saving: true,
                },
                players: super::SpigotSaveableYml {
                    disable_saving: true,
                },
                stats: super::SpigotSaveableYml {
                    disable_saving: true,
                },
                save_user_cache_on_stop_only: true,
            };

            // W
            let yml = config.to_string();

            // T
            insta::assert_snapshot!(yml);
        }
    }
}

mod paper {
    use serde::{Deserialize, Serialize};

    use shulker_crds::v1alpha1::minecraft_server::{
        MinecraftServerConfigurationProxyForwardingMode, MinecraftServerConfigurationSpec,
    };

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct PaperGlobalYml {
        proxies: PaperGlobalProxiesYml,
    }

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct PaperGlobalProxiesYml {
        bungee_cord: PaperGlobalProxiesBungeeCordYml,
        velocity: PaperGlobalProxiesVelocityYml,
    }

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct PaperGlobalProxiesBungeeCordYml {
        online_mode: bool,
    }

    #[derive(Deserialize, Serialize, Clone, Debug)]
    #[serde(rename_all = "kebab-case")]
    pub struct PaperGlobalProxiesVelocityYml {
        enabled: bool,
        online_mode: bool,
        secret: String,
    }

    impl PaperGlobalYml {
        pub fn from_spec(spec: &MinecraftServerConfigurationSpec) -> Self {
            PaperGlobalYml {
                proxies: PaperGlobalProxiesYml {
                    bungee_cord: PaperGlobalProxiesBungeeCordYml {
                        online_mode: spec.proxy_forwarding_mode
                            == MinecraftServerConfigurationProxyForwardingMode::BungeeCord,
                    },
                    velocity: PaperGlobalProxiesVelocityYml {
                        enabled: spec.proxy_forwarding_mode
                            == MinecraftServerConfigurationProxyForwardingMode::Velocity,
                        online_mode: true,
                        secret: "${CFG_VELOCITY_FORWARDING_SECRET}".to_string(),
                    },
                },
            }
        }
    }

    impl ToString for PaperGlobalYml {
        fn to_string(&self) -> String {
            let mut yml = serde_yaml::to_string(&self).unwrap();
            yml.push('\n');

            yml
        }
    }

    #[cfg(test)]
    mod tests {
        use crate::reconcilers::minecraft_server::fixtures::TEST_SERVER;

        #[test]
        fn from_spec() {
            // G
            let spec = TEST_SERVER.spec.config.clone();

            // W
            let config = super::PaperGlobalYml::from_spec(&spec);

            // T
            insta::assert_yaml_snapshot!(config);
        }

        #[test]
        fn to_string() {
            // G
            let config = super::PaperGlobalYml {
                proxies: super::PaperGlobalProxiesYml {
                    bungee_cord: super::PaperGlobalProxiesBungeeCordYml { online_mode: false },
                    velocity: super::PaperGlobalProxiesVelocityYml {
                        enabled: true,
                        online_mode: true,
                        secret: "my-secret".to_string(),
                    },
                },
            };

            // W
            let yml = config.to_string();

            // T
            insta::assert_snapshot!(yml);
        }
    }
}

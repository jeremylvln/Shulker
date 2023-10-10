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

    fn is_updatable() -> bool {
        true
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

    async fn create(
        &self,
        minecraft_server: &Self::OwnerType,
        name: &str,
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
            ..ConfigMap::default()
        };

        Ok(config_map)
    }

    async fn update(
        &self,
        minecraft_server: &Self::OwnerType,
        config_map: &mut Self::ResourceType,
    ) -> Result<(), anyhow::Error> {
        config_map.data = Some(Self::get_data_from_spec(&minecraft_server.spec.config));
        Ok(())
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

mod vanilla {
    use std::collections::HashMap;

    use shulker_crds::v1alpha1::minecraft_server::MinecraftServerConfigurationSpec;

    pub struct VanillaProperties(HashMap<String, String>);

    impl VanillaProperties {
        pub fn from_spec(spec: &MinecraftServerConfigurationSpec) -> Self {
            let mut properties = HashMap::new();

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
}

mod spigot {
    use serde::{Deserialize, Serialize};

    use shulker_crds::v1alpha1::minecraft_server::MinecraftServerConfigurationSpec;

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
                    bungeecord: spec.proxy_forwarding_mode == "BungeeCord",
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
}

mod paper {
    use serde::{Deserialize, Serialize};

    use shulker_crds::v1alpha1::minecraft_server::MinecraftServerConfigurationSpec;

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
                        online_mode: spec.proxy_forwarding_mode == "BungeeCord",
                    },
                    velocity: PaperGlobalProxiesVelocityYml {
                        enabled: spec.proxy_forwarding_mode == "Velocity",
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
}

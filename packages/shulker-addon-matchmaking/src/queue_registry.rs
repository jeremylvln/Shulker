use std::collections::HashMap;

use google_open_match_sdk::{
    function_config, FunctionConfig, MatchProfile, Pool, TagPresentFilter,
};
use kube::ResourceExt;
use shulker_crds::{
    matchmaking::v1alpha1::matchmaking_queue::MatchmakingQueue,
    v1alpha1::minecraft_server_fleet::MinecraftServerFleetRef,
};
use thiserror::Error;
use tracing::info;

use crate::{
    extensions::{set_max_players_in_profile, set_min_players_in_profile},
    mmf::registry::MMFRegistry,
};

#[derive(Debug, Error)]
pub enum RegistryError {
    #[error("built-in mmf {0} not found, it should not be possible")]
    BuiltInMMFNotFound(String),

    #[error("no valid mmf configuration provided for queue {0}")]
    NoValidMMFConfiguration(String),
}

pub struct QueueRegistry {
    mmf_registry: MMFRegistry,
    queues: HashMap<String, PreparedQueue>,
}

impl QueueRegistry {
    pub fn new(mmf_registry: MMFRegistry) -> Self {
        QueueRegistry {
            mmf_registry,
            queues: HashMap::new(),
        }
    }

    pub fn register_queue(
        &mut self,
        matchmaking_queue: &MatchmakingQueue,
    ) -> Result<(), RegistryError> {
        let name = matchmaking_queue.name_any();

        match self.queues.get_mut(&name) {
            Some(prepared_queue) => prepared_queue.update(&self.mmf_registry, matchmaking_queue)?,
            None => {
                self.queues.insert(
                    name.clone(),
                    PreparedQueue::from(&self.mmf_registry, matchmaking_queue)?,
                );
            }
        }

        info!(name = name, "registered queue");
        Ok(())
    }

    pub fn unregister_queue(&mut self, matchmaking_queue: &MatchmakingQueue) {
        let name = matchmaking_queue.name_any();

        self.queues.remove(&name);
        info!(name = name, "unregistered queue");
    }

    pub fn get_queues(&self) -> &HashMap<String, PreparedQueue> {
        &self.queues
    }
}

#[derive(Clone)]
pub struct PreparedQueue {
    pub namespace: String,
    pub fleet_ref: MinecraftServerFleetRef,
    pub mmf_config: FunctionConfig,
    pub match_profile: MatchProfile,
}

impl PreparedQueue {
    pub fn from(
        mmf_registry: &MMFRegistry,
        matchmaking_queue: &MatchmakingQueue,
    ) -> Result<Self, RegistryError> {
        Ok(PreparedQueue {
            namespace: matchmaking_queue.namespace().unwrap(),
            fleet_ref: matchmaking_queue.spec.target_fleet_ref.clone(),
            mmf_config: Self::create_mmf_config(mmf_registry, matchmaking_queue)?,
            match_profile: Self::create_match_profile(matchmaking_queue),
        })
    }

    pub fn update(
        &mut self,
        mmf_registry: &MMFRegistry,
        matchmaking_queue: &MatchmakingQueue,
    ) -> Result<(), RegistryError> {
        self.fleet_ref = matchmaking_queue.spec.target_fleet_ref.clone();
        self.mmf_config = Self::create_mmf_config(mmf_registry, matchmaking_queue)?;
        self.match_profile = Self::create_match_profile(matchmaking_queue);

        Ok(())
    }

    fn create_mmf_config(
        mmf_registry: &MMFRegistry,
        matchmaking_queue: &MatchmakingQueue,
    ) -> Result<FunctionConfig, RegistryError> {
        return if let Some(built_in) = matchmaking_queue.spec.mmf.built_in.as_ref() {
            Ok(mmf_registry
                .get_mmf_config_for_type(&built_in.type_)
                .ok_or_else(|| RegistryError::BuiltInMMFNotFound(built_in.type_.to_string()))?
                .clone())
        } else if let Some(provided) = matchmaking_queue.spec.mmf.provided.as_ref() {
            Ok(FunctionConfig {
                host: provided.host.clone(),
                port: provided.port as i32,
                r#type: function_config::Type::Grpc as i32,
            })
        } else {
            Err(RegistryError::NoValidMMFConfiguration(
                matchmaking_queue.name_any(),
            ))
        };
    }

    fn create_match_profile(matchmaking_queue: &MatchmakingQueue) -> MatchProfile {
        let name = matchmaking_queue.name_any();
        let mut profile = MatchProfile {
            name: format!("shulker_{}", name.replace('-', "_")),
            pools: vec![Pool {
                name: "pool_default".to_string(),
                tag_present_filters: vec![TagPresentFilter { tag: name }],
                ..Pool::default()
            }],
            ..MatchProfile::default()
        };

        if let Some(min_players) = matchmaking_queue.spec.min_players {
            set_min_players_in_profile(&mut profile, min_players as i32);
        }
        set_max_players_in_profile(&mut profile, matchmaking_queue.spec.max_players as i32);

        profile
    }
}

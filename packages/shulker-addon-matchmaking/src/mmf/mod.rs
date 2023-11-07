use std::collections::HashMap;

use google_open_match_sdk::{function_config, FunctionConfig};
use shulker_crds::matchmaking::v1alpha1::matchmaking_queue::MatchmakingQueueMMFBuiltInType;
use tracing::info;

pub mod batch;
pub mod runner;

pub struct MMFRegistry(HashMap<MatchmakingQueueMMFBuiltInType, FunctionConfig>);

impl MMFRegistry {
    pub fn new() -> Self {
        MMFRegistry(HashMap::new())
    }

    pub fn register_mmf(&mut self, type_: MatchmakingQueueMMFBuiltInType, host: String, port: u16) {
        self.0.insert(
            type_.clone(),
            FunctionConfig {
                host: host.clone(),
                port: port as i32,
                r#type: function_config::Type::Grpc as i32,
            },
        );
        info!(
            r#type = type_.to_string(),
            host = host,
            port = port,
            "registered built-in matchmaking function",
        );
    }

    pub fn get_mmf_config_for_type(
        &self,
        type_: &MatchmakingQueueMMFBuiltInType,
    ) -> &FunctionConfig {
        self.0.get(type_).unwrap()
    }
}

impl Default for MMFRegistry {
    fn default() -> Self {
        Self::new()
    }
}

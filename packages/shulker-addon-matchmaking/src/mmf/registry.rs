use std::collections::HashMap;

use google_open_match_sdk::{function_config, FunctionConfig};
use shulker_crds::matchmaking::v1alpha1::matchmaking_queue::MatchmakingQueueMMFBuiltInType;
use tracing::info;

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
    ) -> Option<&FunctionConfig> {
        self.0.get(type_)
    }
}

impl Default for MMFRegistry {
    fn default() -> Self {
        Self::new()
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_register_mmf() {
        // G
        let mut registry = MMFRegistry::new();

        // W
        registry.register_mmf(
            MatchmakingQueueMMFBuiltInType::Batch,
            "localhost".to_string(),
            50505,
        );

        // T
        assert_eq!(
            registry.0,
            HashMap::from([(
                MatchmakingQueueMMFBuiltInType::Batch,
                FunctionConfig {
                    host: "localhost".to_string(),
                    port: 50505,
                    r#type: function_config::Type::Grpc as i32,
                }
            )])
        );
    }

    #[test]
    fn get_mmf_config_for_type_exists() {
        // G
        let original_config = FunctionConfig {
            host: "localhost".to_string(),
            port: 50505,
            r#type: function_config::Type::Grpc as i32,
        };
        let mut registry = MMFRegistry::new();
        registry.0.insert(
            MatchmakingQueueMMFBuiltInType::Batch,
            original_config.clone(),
        );

        // W
        let config = registry.get_mmf_config_for_type(&MatchmakingQueueMMFBuiltInType::Batch);

        // T
        assert_eq!(config, Some(&original_config));
    }

    #[test]
    fn get_mmf_config_for_type_not_exists() {
        // G
        let registry = MMFRegistry::new();

        // W
        let config = registry.get_mmf_config_for_type(&MatchmakingQueueMMFBuiltInType::Batch);

        // T
        assert_eq!(config, None);
    }
}

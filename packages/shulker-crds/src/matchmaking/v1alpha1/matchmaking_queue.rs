use kube::CustomResource;
use schemars::JsonSchema;
use serde::{Deserialize, Serialize};
use strum::{Display, IntoStaticStr};

use crate::v1alpha1::minecraft_server_fleet::MinecraftServerFleetRef;

#[derive(CustomResource, Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[kube(
    kind = "MatchmakingQueue",
    group = "matchmaking.shulkermc.io",
    version = "v1alpha1",
    namespaced,
    status = "MatchmakingQueueStatus",
    printcolumn = r#"{"name": "Age", "type": "date", "jsonPath": ".metadata.creationTimestamp"}"#
)]
#[serde(rename_all = "camelCase")]
pub struct MatchmakingQueueSpec {
    /// The `MinecraftServerFleet` to use as a target for this queue
    pub target_fleet_ref: MinecraftServerFleetRef,

    /// The matchmaking function to use to create matches for this queue
    pub mmf: MatchmakingQueueMMFSpec,

    /// The minimum number of players required to create a match.
    /// If `None`, the matchmaking function will wait for the maximum
    /// number of players
    pub min_players: Option<u32>,

    /// The maximum number of players a match can contain
    pub max_players: u32,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MatchmakingQueueMMFSpec {
    /// The matchmaking function to use is provided by Shulker
    pub built_in: Option<MatchmakingQueueMMFBuiltInSpec>,

    /// The matchmaking function to use is provided by the user
    pub provided: Option<MatchmakingQueueMMFProvidedSpec>,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MatchmakingQueueMMFBuiltInSpec {
    /// The type of the matchmaking function to use
    pub type_: MatchmakingQueueMMFBuiltInType,
}

#[derive(
    PartialEq,
    Eq,
    Hash,
    Deserialize,
    Serialize,
    Clone,
    Debug,
    Default,
    JsonSchema,
    IntoStaticStr,
    Display,
)]
pub enum MatchmakingQueueMMFBuiltInType {
    #[default]
    Batch,
    Elo,
}

#[derive(Deserialize, Serialize, Clone, Debug, Default, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MatchmakingQueueMMFProvidedSpec {
    /// Host of the matchmaking function
    pub host: String,
    /// GRPC port of the matchmaking function
    pub port: u16,
}

/// The status object of `MatchmakingQueue`
#[derive(Deserialize, Serialize, Clone, Debug, JsonSchema)]
#[serde(rename_all = "camelCase")]
pub struct MatchmakingQueueStatus {}

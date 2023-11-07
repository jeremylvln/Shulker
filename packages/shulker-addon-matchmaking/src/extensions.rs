use google_open_match_sdk::{Backfill, MatchProfile};
use pbjson_types::{Int32Value, StringValue};
use prost::Message;

const INT_32_VALUE_TYPE_URL: &str = "type.googleapis.com/google.protobuf.Int32Value";
const STRING_VALUE_TYPE_URL: &str = "type.googleapis.com/google.protobuf.StringValue";

const PROFILE_MIN_PLAYERS_KEY: &str = "shulker:min_players";
const PROFILE_MAX_PLAYERS_KEY: &str = "shulker:max_players";

const BACKFILL_AVAILABLE_SLOTS_KEY: &str = "shulker:available_slots";
const BACKFILL_GAME_SERVER_ID_KEY: &str = "shulker:game_server_id";

macro_rules! encode_any {
    ($typ:ty, $typ_url:ident, $value:expr) => {
        ::pbjson_types::Any {
            type_url: $typ_url.to_string(),
            value: ::prost::bytes::Bytes::from(<$typ>::from($value).encode_to_vec()),
        }
    };
}

macro_rules! try_decode_any {
    ($typ:ty, $typ_url:ident) => {
        |value| match value.type_url.as_str() {
            $typ_url => {
                <$typ>::decode(value.value.as_ref()).map_or_else(|_| None, |x| Some(x.value))
            }
            _ => None,
        }
    };
}

pub fn set_min_players_in_profile(profile: &mut MatchProfile, min_players: i32) {
    profile.extensions.insert(
        PROFILE_MIN_PLAYERS_KEY.to_string(),
        encode_any!(Int32Value, INT_32_VALUE_TYPE_URL, min_players),
    );
}

pub fn get_min_players_from_profile(profile: &MatchProfile) -> Option<i32> {
    profile
        .extensions
        .get(PROFILE_MIN_PLAYERS_KEY)
        .and_then(try_decode_any!(Int32Value, INT_32_VALUE_TYPE_URL))
}

pub fn set_max_players_in_profile(profile: &mut MatchProfile, max_players: i32) {
    profile.extensions.insert(
        PROFILE_MAX_PLAYERS_KEY.to_string(),
        encode_any!(Int32Value, INT_32_VALUE_TYPE_URL, max_players),
    );
}

pub fn get_max_players_from_profile(profile: &MatchProfile) -> Option<i32> {
    profile
        .extensions
        .get(PROFILE_MAX_PLAYERS_KEY)
        .and_then(try_decode_any!(Int32Value, INT_32_VALUE_TYPE_URL))
}

pub fn set_available_slots_in_backfill(backfill: &mut Backfill, available_slots: i32) {
    backfill.extensions.insert(
        BACKFILL_AVAILABLE_SLOTS_KEY.to_string(),
        encode_any!(Int32Value, INT_32_VALUE_TYPE_URL, available_slots),
    );
}

pub fn get_available_slots_from_backfill(backfill: &Backfill) -> Option<i32> {
    backfill
        .extensions
        .get(BACKFILL_AVAILABLE_SLOTS_KEY)
        .and_then(try_decode_any!(Int32Value, INT_32_VALUE_TYPE_URL))
}

pub fn set_game_server_id_in_backfill(backfill: &mut Backfill, game_server_id: String) {
    backfill.extensions.insert(
        BACKFILL_GAME_SERVER_ID_KEY.to_string(),
        encode_any!(StringValue, STRING_VALUE_TYPE_URL, game_server_id),
    );
}

pub fn get_game_server_id_from_backfill(backfill: &Backfill) -> Option<String> {
    backfill
        .extensions
        .get(BACKFILL_GAME_SERVER_ID_KEY)
        .and_then(try_decode_any!(StringValue, STRING_VALUE_TYPE_URL))
}

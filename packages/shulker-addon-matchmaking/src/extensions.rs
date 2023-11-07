use google_open_match_sdk::{Backfill, MatchProfile};
use prost::Message;

const PROFILE_MIN_PLAYERS_KEY: &str = "shulker:min_players";
const PROFILE_MAX_PLAYERS_KEY: &str = "shulker:max_players";
const BACKFILL_AVAILABLE_SLOTS_KEY: &str = "shulker:available_slots";
const BACKFILL_GAME_SERVER_ID_KEY: &str = "shulker:game_server_id";

macro_rules! encode_any {
    ($typ:ty, $typ_url:literal, $value:expr) => {
        ::pbjson_types::Any {
            type_url: $typ_url.to_string(),
            value: ::prost::bytes::Bytes::from(<$typ>::from($value).encode_to_vec()),
        }
    };
    (i32, $value:expr) => {
        encode_any!(
            pbjson_types::Int32Value,
            "type.googleapis.com/google.protobuf.Int32Value",
            $value
        )
    };
    (String, $value:expr) => {
        encode_any!(
            pbjson_types::StringValue,
            "type.googleapis.com/google.protobuf.StringValue",
            $value
        )
    };
}

macro_rules! try_decode_any {
    ($typ:ty, $typ_url:literal) => {
        |value| match value.type_url.as_str() {
            $typ_url => {
                <$typ>::decode(value.value.as_ref()).map_or_else(|_| None, |x| Some(x.value))
            }
            _ => None,
        }
    };
    (i32) => {
        try_decode_any!(
            pbjson_types::Int32Value,
            "type.googleapis.com/google.protobuf.Int32Value"
        )
    };
    (String) => {
        try_decode_any!(
            pbjson_types::StringValue,
            "type.googleapis.com/google.protobuf.StringValue"
        )
    };
}

macro_rules! create_extension_encoder_decoder {
    ($target:ty, $target_name:literal, $typ:ty, $name:literal, $key:ident) => {
        paste::item! {
            pub fn [< set_ $name _in_ $target_name >] (target: &mut $target, value: $typ) {
                target.extensions.insert(
                    $key.to_string(),
                    encode_any!($typ, value),
                );
            }

            pub fn [< get_ $name _from_ $target_name >] (target: &$target) -> Option<$typ> {
                target
                    .extensions
                    .get($key)
                    .and_then(try_decode_any!($typ))
            }
        }
    };
}

create_extension_encoder_decoder!(
    MatchProfile,
    "profile",
    i32,
    "min_players",
    PROFILE_MIN_PLAYERS_KEY
);

create_extension_encoder_decoder!(
    MatchProfile,
    "profile",
    i32,
    "max_players",
    PROFILE_MAX_PLAYERS_KEY
);

create_extension_encoder_decoder!(
    Backfill,
    "backfill",
    i32,
    "available_slots",
    BACKFILL_AVAILABLE_SLOTS_KEY
);

create_extension_encoder_decoder!(
    Backfill,
    "backfill",
    String,
    "game_server_id",
    BACKFILL_GAME_SERVER_ID_KEY
);

#[cfg(test)]
mod tests {
    use google_open_match_sdk::{Backfill, MatchProfile};
    use prost::Message;

    macro_rules! create_encoder_decoder_tests {
        ($target:ty, $target_name:literal, $typ:ty, $name:literal, $key:literal, $value:expr) => {
            paste::item! {
                #[test]
                fn [< set_ $name _in_ $target_name >] () {
                    // G
                    let mut target = <$target>::default();

                    // W
                    super::[< set_ $name _in_ $target_name >](&mut target, $value);

                    // T
                    assert_eq!(
                        target.extensions.get($key),
                        Some(&encode_any!($typ, $value))
                    );
                }

                #[test]
                fn [< get_ $name _from_ $target_name _exists >] () {
                    // G
                    let mut target = <$target>::default();
                    target.extensions.insert($key.to_string(), encode_any!($typ, $value));

                    // W
                    let value = super::[< get_ $name _from_ $target_name >](&target);

                    // T
                    assert_eq!(value.unwrap(), $value);
                }

                #[test]
                fn [< get_ $name _from_ $target_name _not_exists >] () {
                    // G
                    let target = <$target>::default();

                    // W
                    let value = super::[< get_ $name _from_ $target_name >](&target);

                    // T
                    assert_eq!(value, None);
                }
            }
        };
    }

    create_encoder_decoder_tests!(
        MatchProfile,
        "profile",
        i32,
        "min_players",
        "shulker:min_players",
        42
    );

    create_encoder_decoder_tests!(
        MatchProfile,
        "profile",
        i32,
        "max_players",
        "shulker:max_players",
        42
    );

    create_encoder_decoder_tests!(
        Backfill,
        "backfill",
        i32,
        "available_slots",
        "shulker:available_slots",
        42
    );

    create_encoder_decoder_tests!(
        Backfill,
        "backfill",
        String,
        "game_server_id",
        "shulker:game_server_id",
        "randomid".to_string()
    );
}

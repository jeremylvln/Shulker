use google_open_match_sdk::{MatchProfile, Ticket};
use uuid::Uuid;

use crate::extensions::{set_max_players_in_profile, set_min_players_in_profile};

pub fn create_random_profile(min_players: Option<i32>, max_players: i32) -> MatchProfile {
    let mut profile = MatchProfile {
        name: Uuid::new_v4().to_string(),
        ..MatchProfile::default()
    };
    if let Some(min_players) = min_players {
        set_min_players_in_profile(&mut profile, min_players);
    }
    set_max_players_in_profile(&mut profile, max_players);

    profile
}

pub fn create_random_ticket() -> Ticket {
    Ticket {
        id: Uuid::new_v4().to_string(),
        ..Ticket::default()
    }
}

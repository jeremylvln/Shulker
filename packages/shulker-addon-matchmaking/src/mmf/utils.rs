use google_open_match_sdk::{Backfill, Match, MatchProfile, Pool, SearchFields, Ticket};
use pbjson_types::Timestamp;
use uuid::Uuid;

use crate::extensions::set_available_slots_in_backfill;

pub fn create_full_match(
    profile: &MatchProfile,
    function_name: String,
    tickets: Vec<Ticket>,
) -> Match {
    Match {
        match_id: Uuid::new_v4().to_string(),
        match_profile: profile.name.clone(),
        match_function: function_name,
        tickets,
        ..Match::default()
    }
}

pub fn create_match_with_backfill(
    profile: &MatchProfile,
    function_name: String,
    tickets: Vec<Ticket>,
    backfill: Backfill,
    allocate_gameserver: bool,
) -> Match {
    let mut created_match = create_full_match(profile, function_name, tickets);
    created_match.backfill = Some(backfill);
    created_match.allocate_gameserver = allocate_gameserver;
    created_match
}

pub fn create_backfill_for_pool(pool: &Pool, remaining_slots: i32) -> Backfill {
    let mut backfill = Backfill {
        generation: 0,
        create_time: Some(Timestamp {
            seconds: shulker_utils::time::now().timestamp(),
            nanos: 0,
        }),
        search_fields: Some(SearchFields {
            tags: pool
                .tag_present_filters
                .iter()
                .map(|f| f.tag.clone())
                .collect(),
            ..SearchFields::default()
        }),
        ..Backfill::default()
    };
    set_available_slots_in_backfill(&mut backfill, remaining_slots);

    backfill
}

#[cfg(test)]
mod tests {
    use google_open_match_sdk::{Backfill, Pool, SearchFields, TagPresentFilter};
    use uuid::Uuid;

    use crate::{
        extensions::get_available_slots_from_backfill,
        mmf::fixtures::{create_random_profile, create_random_ticket},
    };

    #[test]
    fn create_full_match() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let function_name = "test".to_string();
        let tickets = vec![create_random_ticket(), create_random_ticket()];

        // W
        let created_match =
            super::create_full_match(&profile, function_name.clone(), tickets.clone());

        // T
        assert_eq!(created_match.match_profile, profile.name);
        assert_eq!(created_match.match_function, function_name);
        assert_eq!(created_match.tickets, tickets);
        assert_eq!(created_match.backfill, None);
        assert!(!created_match.allocate_gameserver);
        assert!(created_match.extensions.is_empty());
    }

    #[test]
    fn create_match_with_backfill() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let function_name = "test".to_string();
        let tickets = vec![create_random_ticket(), create_random_ticket()];
        let backfill = Backfill {
            id: Uuid::new_v4().to_string(),
            ..Backfill::default()
        };

        // W
        let created_match = super::create_match_with_backfill(
            &profile,
            function_name.clone(),
            tickets.clone(),
            backfill.clone(),
            true,
        );

        // T
        assert_eq!(created_match.match_profile, profile.name);
        assert_eq!(created_match.match_function, function_name);
        assert_eq!(created_match.tickets, tickets);
        assert_eq!(created_match.backfill, Some(backfill));
        assert!(created_match.allocate_gameserver);
        assert!(created_match.extensions.is_empty());
    }

    #[test]
    fn create_backfill_for_pool() {
        // G
        let pool = Pool {
            name: "pool".to_string(),
            tag_present_filters: vec![TagPresentFilter {
                tag: "tag".to_string(),
            }],
            ..Pool::default()
        };

        // W
        let backfill = super::create_backfill_for_pool(&pool, 4);

        // T
        assert_eq!(get_available_slots_from_backfill(&backfill), Some(4));
        assert_eq!(
            backfill.search_fields,
            Some(SearchFields {
                tags: vec!["tag".to_string()],
                ..SearchFields::default()
            })
        );
    }
}

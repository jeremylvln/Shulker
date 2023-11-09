use std::{cmp::min, pin::Pin};

use futures::Stream;
use google_open_match_sdk::{
    match_function_server::MatchFunction, query_service_client::QueryServiceClient, Backfill,
    Match, MatchProfile, Pool, RunRequest, RunResponse, Ticket,
};
use tonic::{transport::Channel, Response, Status};
use tracing::*;

use crate::extensions::{
    get_available_slots_from_backfill, get_max_players_from_profile, get_min_players_from_profile,
    set_available_slots_in_backfill,
};

use super::{
    runner::MatchSupplier,
    utils::{create_backfill_for_pool, create_full_match, create_match_with_backfill},
};

const MMF_NAME: &str = "shulker-built-in-mmf-batch";

pub struct MatchFunctionBatch {
    query_client: QueryServiceClient<Channel>,
}

impl MatchFunctionBatch {
    pub fn new(query_client: QueryServiceClient<Channel>) -> Self {
        MatchFunctionBatch { query_client }
    }

    fn fill_existing_backfills(
        profile: &MatchProfile,
        mut backfills: Vec<Backfill>,
        mut tickets: Vec<Ticket>,
    ) -> (Vec<Match>, Vec<Ticket>) {
        let mut matches = vec![];

        for backfill in backfills.iter_mut() {
            let available_slots = get_available_slots_from_backfill(backfill).unwrap();

            let tickets_to_drain = min(available_slots as usize, tickets.len());
            let match_tickets: Vec<Ticket> = tickets.drain(0..tickets_to_drain).collect();

            if !match_tickets.is_empty() {
                let remaining_slots = available_slots - match_tickets.len() as i32;
                set_available_slots_in_backfill(backfill, remaining_slots);

                matches.push(create_match_with_backfill(
                    profile,
                    MMF_NAME.to_string(),
                    match_tickets,
                    backfill.clone(),
                    false,
                ));
            }
        }

        (matches, tickets)
    }

    fn create_full_matches(
        profile: &MatchProfile,
        mut tickets: Vec<Ticket>,
    ) -> (Vec<Match>, Vec<Ticket>) {
        let mut matches = vec![];
        let max_players_per_match = get_max_players_from_profile(profile).unwrap();

        while tickets.len() >= max_players_per_match as usize {
            let match_tickets = tickets.drain(0..max_players_per_match as usize).collect();
            matches.push(create_full_match(
                profile,
                MMF_NAME.to_string(),
                match_tickets,
            ));
        }

        (matches, tickets)
    }

    fn create_last_partial_match(
        profile: &MatchProfile,
        pool: &Pool,
        mut tickets: Vec<Ticket>,
    ) -> Option<Match> {
        let max_players_per_match = get_max_players_from_profile(profile).unwrap();
        let min_players_per_match =
            get_min_players_from_profile(profile).unwrap_or(max_players_per_match);

        if tickets.len() < min_players_per_match as usize {
            debug!(
                profile_name = profile.name,
                pool_name = pool.name,
                min_players_per_match = min_players_per_match,
                tickets_count = tickets.len(),
                "not enough tickets to create a partial match"
            );
            return None;
        }

        let partial_match_size = min(tickets.len(), max_players_per_match as usize);
        let match_tickets: Vec<Ticket> = tickets.drain(0..partial_match_size).collect();
        let remaining_slots = max_players_per_match - match_tickets.len() as i32;
        let backfill = create_backfill_for_pool(pool, remaining_slots);

        Some(create_match_with_backfill(
            profile,
            MMF_NAME.to_string(),
            match_tickets,
            backfill,
            true,
        ))
    }
}

impl MatchSupplier for MatchFunctionBatch {
    fn create_matches(
        &self,
        profile: &MatchProfile,
        pool: &Pool,
        tickets: Vec<Ticket>,
        backfills: Vec<Backfill>,
    ) -> Vec<Match> {
        let mut matches = vec![];

        let (mut backfill_matches, remaining_tickets) =
            Self::fill_existing_backfills(profile, backfills, tickets);
        matches.append(&mut backfill_matches);

        let (mut full_matches, remaining_tickets) =
            Self::create_full_matches(profile, remaining_tickets);
        matches.append(&mut full_matches);

        if !remaining_tickets.is_empty() {
            if let Some(last_partial_match) =
                Self::create_last_partial_match(profile, pool, remaining_tickets)
            {
                matches.push(last_partial_match);
            }
        }

        matches
    }
}

super::runner::create_match_function_runner!(MatchFunctionBatch);

#[cfg(test)]
mod tests {
    use google_open_match_sdk::Pool;

    use crate::{
        extensions::set_available_slots_in_backfill,
        mmf::{
            fixtures::{create_random_profile, create_random_ticket},
            utils::create_backfill_for_pool,
        },
    };

    use super::MatchFunctionBatch;

    #[test]
    fn fill_existing_backfills_tickets_and_backfill_perfect_amount() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let pool = Pool::default();
        let backfills = vec![create_backfill_for_pool(&pool, 2)];
        let tickets = vec![create_random_ticket(), create_random_ticket()];

        // W
        let (matches, remaining_tickets) = MatchFunctionBatch::fill_existing_backfills(
            &profile,
            backfills.clone(),
            tickets.clone(),
        );

        // T
        assert_eq!(matches.len(), 1);
        let first_match = matches.first().unwrap();
        assert_eq!(first_match.tickets, tickets);
        assert_eq!(first_match.backfill, {
            let mut backfill = backfills.first().unwrap().clone();
            set_available_slots_in_backfill(&mut backfill, 0);
            Some(backfill)
        });
        assert!(!first_match.allocate_gameserver);
        assert!(remaining_tickets.is_empty());
    }

    #[test]
    fn fill_existing_backfills_tickets_and_backfill_more_than_enough() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let pool = Pool::default();
        let backfills = vec![create_backfill_for_pool(&pool, 2)];
        let tickets = vec![
            create_random_ticket(),
            create_random_ticket(),
            create_random_ticket(),
            create_random_ticket(),
        ];

        // W
        let (matches, remaining_tickets) = MatchFunctionBatch::fill_existing_backfills(
            &profile,
            backfills.clone(),
            tickets.clone(),
        );

        // T
        assert_eq!(matches.len(), 1);
        let first_match = matches.first().unwrap();
        assert_eq!(
            first_match.tickets,
            Vec::from_iter(tickets[0..2].iter().cloned())
        );
        assert_eq!(first_match.backfill, {
            let mut backfill = backfills.first().unwrap().clone();
            set_available_slots_in_backfill(&mut backfill, 0);
            Some(backfill)
        });
        assert!(!first_match.allocate_gameserver);
        assert_eq!(
            remaining_tickets,
            Vec::from_iter(tickets[2..4].iter().cloned())
        );
    }

    #[test]
    fn fill_existing_backfills_tickets_and_backfill_not_enough() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let pool = Pool::default();
        let backfills = vec![create_backfill_for_pool(&pool, 2)];
        let tickets = vec![create_random_ticket()];

        // W
        let (matches, remaining_tickets) = MatchFunctionBatch::fill_existing_backfills(
            &profile,
            backfills.clone(),
            tickets.clone(),
        );

        // T
        assert_eq!(matches.len(), 1);
        let first_match = matches.first().unwrap();
        assert_eq!(first_match.tickets, tickets);
        assert_eq!(first_match.backfill, {
            let mut backfill = backfills.first().unwrap().clone();
            set_available_slots_in_backfill(&mut backfill, 1);
            Some(backfill)
        });
        assert!(!first_match.allocate_gameserver);
        assert!(remaining_tickets.is_empty());
    }

    #[test]
    fn fill_existing_backfills_tickets_no_backfills() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let backfills = vec![];
        let tickets = vec![create_random_ticket()];

        // W
        let (matches, remaining_tickets) =
            MatchFunctionBatch::fill_existing_backfills(&profile, backfills, tickets.clone());

        // T
        assert!(matches.is_empty());
        assert_eq!(remaining_tickets, tickets);
    }

    #[test]
    fn fill_existing_backfills_no_tickets_existing_backfills() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let pool = Pool::default();
        let backfills = vec![create_backfill_for_pool(&pool, 8)];
        let tickets = vec![];

        // W
        let (matches, remaining_tickets) =
            MatchFunctionBatch::fill_existing_backfills(&profile, backfills, tickets);

        // T
        assert!(matches.is_empty());
        assert!(remaining_tickets.is_empty());
    }

    #[test]
    fn fill_existing_backfills_no_tickets_no_backfills() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let backfills = vec![];
        let tickets = vec![];

        // W
        let (matches, remaining_tickets) =
            MatchFunctionBatch::fill_existing_backfills(&profile, backfills, tickets);

        // T
        assert!(matches.is_empty());
        assert!(remaining_tickets.is_empty());
    }

    #[test]
    fn create_full_matches_perfect_amount() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let tickets = vec![
            create_random_ticket(),
            create_random_ticket(),
            create_random_ticket(),
            create_random_ticket(),
        ];

        // W
        let (matches, remaining_tickets) =
            MatchFunctionBatch::create_full_matches(&profile, tickets.clone());

        // T
        assert_eq!(matches.len(), 1);
        let first_match = matches.first().unwrap();
        assert_eq!(first_match.tickets, tickets);
        assert_eq!(first_match.backfill, None);
        assert!(!first_match.allocate_gameserver);
        assert!(remaining_tickets.is_empty());
    }

    #[test]
    fn create_full_matches_perfect_more_than_enough() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let tickets = vec![
            create_random_ticket(),
            create_random_ticket(),
            create_random_ticket(),
            create_random_ticket(),
            create_random_ticket(),
        ];

        // W
        let (matches, remaining_tickets) =
            MatchFunctionBatch::create_full_matches(&profile, tickets.clone());

        // T
        assert_eq!(matches.len(), 1);
        let first_match = matches.first().unwrap();
        assert_eq!(
            first_match.tickets,
            Vec::from_iter(tickets[0..4].iter().cloned())
        );
        assert_eq!(first_match.backfill, None);
        assert!(!first_match.allocate_gameserver);
        assert_eq!(remaining_tickets, vec![tickets[4].clone()]);
    }

    #[test]
    fn create_full_matches_not_enough() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let tickets = vec![create_random_ticket()];

        // W
        let (matches, remaining_tickets) =
            MatchFunctionBatch::create_full_matches(&profile, tickets.clone());

        // T
        assert!(matches.is_empty());
        assert_eq!(remaining_tickets, tickets);
    }

    #[test]
    fn create_last_partial_match_more_than_minimum() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let pool = Pool::default();
        let tickets = vec![
            create_random_ticket(),
            create_random_ticket(),
            create_random_ticket(),
        ];

        // W
        let last_match =
            MatchFunctionBatch::create_last_partial_match(&profile, &pool, tickets.clone())
                .unwrap();

        // T
        assert_eq!(
            last_match.tickets,
            Vec::from_iter(tickets[0..3].iter().cloned())
        );
        assert_eq!(
            last_match.backfill,
            Some(create_backfill_for_pool(&pool, 1))
        );
        assert!(last_match.allocate_gameserver);
    }

    #[test]
    fn create_last_partial_match_less_than_minimum() {
        // G
        let profile = create_random_profile(Some(2), 4);
        let pool = Pool::default();
        let tickets = vec![create_random_ticket()];

        // W
        let last_match =
            MatchFunctionBatch::create_last_partial_match(&profile, &pool, tickets.clone());

        // T
        assert_eq!(last_match, None);
    }
}

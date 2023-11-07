use std::{cmp::min, pin::Pin};

use futures::Stream;
use google_open_match_sdk::{
    match_function_server::MatchFunction, query_service_client::QueryServiceClient, Backfill,
    Match, MatchProfile, Pool, RunRequest, RunResponse, SearchFields, Ticket,
};
use pbjson_types::Timestamp;
use tonic::{transport::Channel, Response, Status};
use tracing::*;
use uuid::Uuid;

use crate::extensions::{
    get_available_slots_from_backfill, get_max_players_from_profile, get_min_players_from_profile,
    set_available_slots_in_backfill,
};

use super::runner::MatchSupplier;

const MMF_NAME: &str = "shulker-built-in-mmf-batch";

pub struct MatchFunctionBatch {
    query_client: QueryServiceClient<Channel>,
}

impl MatchFunctionBatch {
    pub fn new(query_client: QueryServiceClient<Channel>) -> Self {
        MatchFunctionBatch { query_client }
    }

    fn fill_backfills(
        profile: &MatchProfile,
        mut backfills: Vec<Backfill>,
        mut tickets: Vec<Ticket>,
    ) -> (Vec<Match>, Vec<Ticket>) {
        let mut proposals = vec![];

        for backfill in backfills.iter_mut() {
            let available_slots = get_available_slots_from_backfill(backfill).unwrap();

            let mut proposal_tickets = vec![];
            if available_slots > 0 && !tickets.is_empty() {
                for _ in 0..available_slots {
                    proposal_tickets.push(tickets.remove(0));
                }
            }

            if !proposal_tickets.is_empty() {
                let remaining_slots = available_slots - proposal_tickets.len() as i32;
                set_available_slots_in_backfill(backfill, remaining_slots);

                let proposal = Match {
                    match_id: Uuid::new_v4().to_string(),
                    match_profile: profile.name.clone(),
                    match_function: MMF_NAME.to_string(),
                    tickets: proposal_tickets,
                    backfill: Some(backfill.clone()),
                    allocate_gameserver: false,
                    ..Match::default()
                };

                proposals.push(proposal);
            }
        }

        (proposals, tickets)
    }

    fn create_full_matches(
        profile: &MatchProfile,
        mut tickets: Vec<Ticket>,
    ) -> (Vec<Match>, Vec<Ticket>) {
        let mut proposals = vec![];
        let max_players_per_match = get_max_players_from_profile(profile).unwrap();

        while tickets.len() >= max_players_per_match as usize {
            let proposal_tickets = tickets.drain(0..max_players_per_match as usize).collect();

            let proposal = Match {
                match_id: Uuid::new_v4().to_string(),
                match_profile: profile.name.clone(),
                match_function: MMF_NAME.to_string(),
                tickets: proposal_tickets,
                ..Match::default()
            };

            proposals.push(proposal);
        }

        (proposals, tickets)
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
        let proposal_tickets: Vec<Ticket> = tickets.drain(0..partial_match_size).collect();
        let backfill_size = max_players_per_match - proposal_tickets.len() as i32;

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
        set_available_slots_in_backfill(&mut backfill, backfill_size);

        Some(Match {
            match_id: Uuid::new_v4().to_string(),
            match_profile: profile.name.clone(),
            match_function: MMF_NAME.to_string(),
            tickets: proposal_tickets,
            backfill: Some(backfill),
            allocate_gameserver: true,
            ..Match::default()
        })
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
        let mut proposals = vec![];

        let (mut p1, t1) = Self::fill_backfills(profile, backfills, tickets);
        proposals.append(&mut p1);

        let (mut p2, t2) = Self::create_full_matches(profile, t1);
        proposals.append(&mut p2);

        if !t2.is_empty() {
            if let Some(p3) = Self::create_last_partial_match(profile, pool, t2) {
                proposals.push(p3);
            }
        }

        proposals
    }
}

super::runner::create_match_function_runner!(MatchFunctionBatch);

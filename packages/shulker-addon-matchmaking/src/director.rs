use std::{
    collections::HashMap,
    sync::{Arc, Mutex},
};

use futures::StreamExt;
use google_open_match_sdk::{
    backend_service_client::BackendServiceClient, AssignTicketsRequest, Assignment,
    AssignmentGroup, FetchMatchesRequest, Match,
};
use shulker_sdk::{sdk_service_client::SdkServiceClient, FleetAllocationRequest};
use thiserror::Error;
use tokio_util::sync::CancellationToken;
use tonic::transport::Channel;
use tracing::*;

use crate::{
    extensions::get_game_server_id_from_backfill,
    queue_registry::{PreparedQueue, QueueRegistry},
};

const DIRECTOR_MATCH_POLL_INTERVAL_SECONDS: u64 = 5;

#[derive(Debug, Error)]
enum DirectorError {
    #[error("the provided backfill does not have a game server id")]
    BackfillWithoutGameServerId,
}

#[derive(Clone)]
struct Context {
    open_match_backend_client: BackendServiceClient<Channel>,
    shulker_sdk_client: SdkServiceClient<Channel>,
    queue_registry: Arc<Mutex<QueueRegistry>>,
}

pub fn run(
    open_match_backend_client: BackendServiceClient<Channel>,
    shulker_sdk_client: SdkServiceClient<Channel>,
    queue_registry: Arc<Mutex<QueueRegistry>>,
    cancellation_token: CancellationToken,
) -> Result<tokio::task::JoinHandle<()>, anyhow::Error> {
    let context = Context {
        open_match_backend_client,
        shulker_sdk_client,
        queue_registry,
    };

    let task = tokio::spawn(async move {
        let mut interval = tokio::time::interval(tokio::time::Duration::from_secs(
            DIRECTOR_MATCH_POLL_INTERVAL_SECONDS,
        ));
        interval.tick().await;

        loop {
            tokio::select! {
                _ = interval.tick() => {
                    let res = try_fetch_matches_for_all_queues(&context).await;

                    if let Err(err) = res {
                        tracing::error!("failed to assign matches: {:?}", err);
                    }
                }
                _ = cancellation_token.cancelled() => {
                    break
                }
            }
        }
    });

    Ok(task)
}

async fn try_fetch_matches_for_all_queues(context: &Context) -> Result<(), anyhow::Error> {
    debug!("fetching matches for all queues");

    let prepared_queues = {
        let queue_registry = context
            .queue_registry
            .try_lock()
            .map_err(|_| anyhow::anyhow!("failed to lock queue registry"))?;

        queue_registry.get_queues().clone()
    };

    for (name, queue) in prepared_queues {
        try_fetch_matches_for_queue(context, &name, &queue).await?;
    }

    Ok(())
}

async fn try_fetch_matches_for_queue(
    context: &Context,
    queue_name: &str,
    queue: &PreparedQueue,
) -> Result<(), anyhow::Error> {
    debug!(name = queue_name, "fetching matches for queue");

    let mut stream = context
        .open_match_backend_client
        .clone()
        .fetch_matches(FetchMatchesRequest {
            config: Some(queue.mmf_config.clone()),
            profile: Some(queue.match_profile.clone()),
        })
        .await?
        .into_inner();

    while let Some(res) = stream.next().await {
        let created_match = res.unwrap().r#match.unwrap();
        try_allocate_match(context, queue, created_match).await?;
    }

    Ok(())
}

async fn try_allocate_match(
    context: &Context,
    queue: &PreparedQueue,
    created_match: Match,
) -> Result<(), anyhow::Error> {
    info!(
        match_id = created_match.match_id,
        match_profile = created_match.match_profile,
        has_backfill = created_match.backfill.is_some(),
        "trying to allocate match"
    );

    let game_server_id = if created_match.allocate_gameserver || created_match.backfill.is_none() {
        debug!(
            match_id = created_match.match_id,
            match_profile = created_match.match_profile,
            "match needs a new server to be allocated"
        );

        context
            .shulker_sdk_client
            .clone()
            .allocate_from_fleet(FleetAllocationRequest {
                namespace: queue.namespace.to_string(),
                name: queue.fleet_ref.name.clone(),
                summon_if_needed: true,
                custom_annotations: match &created_match.backfill {
                    Some(backfill) => HashMap::from([(
                        "matchmaking.shulkermc.io/backfill".to_string(),
                        backfill.id.clone(),
                    )]),
                    None => HashMap::new(),
                },
            })
            .await?
            .into_inner()
            .game_server_id
    } else {
        created_match
            .backfill
            .as_ref()
            .and_then(get_game_server_id_from_backfill)
            .ok_or(DirectorError::BackfillWithoutGameServerId)?
    };

    info!(
        match_id = created_match.match_id,
        match_profile = created_match.match_profile,
        has_backfill = created_match.backfill.is_some(),
        "trying to allocate match"
    );

    context
        .open_match_backend_client
        .clone()
        .assign_tickets(AssignTicketsRequest {
            assignments: vec![AssignmentGroup {
                ticket_ids: created_match
                    .tickets
                    .iter()
                    .map(|ticket| ticket.id.clone())
                    .collect(),
                assignment: Some(Assignment {
                    connection: game_server_id,
                    ..Assignment::default()
                }),
            }],
        })
        .await?;

    Ok(())
}

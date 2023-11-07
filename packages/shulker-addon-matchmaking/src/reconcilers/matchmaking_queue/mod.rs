use std::{
    sync::{Arc, Mutex},
    time::Duration,
};

use futures::StreamExt;
use kube::{
    api::ListParams,
    runtime::{controller::Action, watcher::Config, Controller},
    Api, Client, ResourceExt,
};
use tracing::*;

use shulker_crds::matchmaking::v1alpha1::matchmaking_queue::MatchmakingQueue;

use crate::queue_registry::QueueRegistry;

use super::{ReconcilerError, Result};

struct MatchmakingQueueReconciler {
    client: kube::Client,
    queue_registry: Arc<Mutex<QueueRegistry>>,
}

impl MatchmakingQueueReconciler {
    async fn reconcile(
        &self,
        _api: Api<MatchmakingQueue>,
        matchmaking_queue: Arc<MatchmakingQueue>,
    ) -> Result<Action> {
        self.queue_registry
            .lock()
            .unwrap()
            .register_queue(&matchmaking_queue)
            .map_err(ReconcilerError::FailedToRegisterQueue)?;

        Ok(Action::requeue(Duration::from_secs(5 * 60)))
    }

    async fn cleanup(&self, matchmaking_queue: Arc<MatchmakingQueue>) -> Result<Action> {
        info!(
            name = matchmaking_queue.name_any(),
            namespace = matchmaking_queue.namespace(),
            "cleaning up MatchmakingQueue",
        );

        self.queue_registry
            .lock()
            .unwrap()
            .unregister_queue(&matchmaking_queue);

        Ok(Action::await_change())
    }
}

#[instrument(skip(ctx, matchmaking_queue))]
async fn reconcile(
    matchmaking_queue: Arc<MatchmakingQueue>,
    ctx: Arc<MatchmakingQueueReconciler>,
) -> Result<Action> {
    let ns = matchmaking_queue.namespace().unwrap();
    let matchmaking_queues_api: Api<MatchmakingQueue> = Api::namespaced(ctx.client.clone(), &ns);

    info!(
        name = matchmaking_queue.name_any(),
        namespace = ns,
        "reconciling MatchmakingQueue",
    );

    if matchmaking_queue.metadata.deletion_timestamp.is_none() {
        ctx.reconcile(matchmaking_queues_api.clone(), matchmaking_queue.clone())
            .await
    } else {
        ctx.cleanup(matchmaking_queue.clone()).await
    }
}

fn error_policy(
    _matchmaking_queue: Arc<MatchmakingQueue>,
    error: &ReconcilerError,
    _ctx: Arc<MatchmakingQueueReconciler>,
) -> Action {
    warn!("reconcile failed: {:?}", error);
    // ctx.metrics.reconcile_failure(&matchmaking_queue, error);
    Action::requeue(Duration::from_secs(5))
}

pub async fn run<'a>(client: Client, queue_registry: Arc<Mutex<QueueRegistry>>) {
    let matchmaking_queues_api = Api::<MatchmakingQueue>::all(client.clone());
    if let Err(e) = matchmaking_queues_api
        .list(&ListParams::default().limit(1))
        .await
    {
        error!("CRD is not queryable; {e:?}. Is the CRD installed?");
        std::process::exit(1);
    }

    let context = MatchmakingQueueReconciler {
        client: client.clone(),
        queue_registry,
    };

    Controller::new(matchmaking_queues_api, Config::default().any_semantic())
        .shutdown_on_signal()
        .run(reconcile, error_policy, context.into())
        .filter_map(|x| async move { std::result::Result::ok(x) })
        .for_each(|_| futures::future::ready(()))
        .await;
}

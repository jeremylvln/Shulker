use std::{collections::BTreeMap, sync::Arc, time::Duration};

use futures::StreamExt;
use google_agones_crds::v1::game_server::GameServer;
use k8s_openapi::api::core::v1::ConfigMap;
use kube::{
    api::{DeleteParams, ListParams, PatchParams},
    runtime::{controller::Action, watcher::Config, Controller},
    Api, Client, ResourceExt,
};
use shulker_kube_utils::reconcilers::{builder::reconcile_builder, status::patch_status};
use tracing::*;

use shulker_crds::{
    condition::{ConditionStatus, HasConditions},
    v1alpha1::minecraft_server::{MinecraftServer, MinecraftServerStatus},
};

use crate::agent::AgentConfig;

use self::{
    config_map::ConfigMapBuilder,
    gameserver::{GameServerBuilder, GameServerBuilderContext},
};

use super::{cluster_ref::resolve_cluster_ref, ReconcilerError, Result};

pub mod config_map;
pub mod gameserver;

#[cfg(test)]
mod fixtures;

struct MinecraftServerReconciler {
    client: kube::Client,
    agent_config: AgentConfig,

    // Builders
    config_map_builder: ConfigMapBuilder,
    gameserver_builder: GameServerBuilder,
}

impl MinecraftServerReconciler {
    async fn reconcile(
        &self,
        api: Api<MinecraftServer>,
        minecraft_server: Arc<MinecraftServer>,
    ) -> Result<Action> {
        let cluster = resolve_cluster_ref(
            &self.client,
            &minecraft_server.namespace().unwrap(),
            &minecraft_server.spec.cluster_ref,
        )
        .await?;

        reconcile_builder(&self.config_map_builder, minecraft_server.as_ref(), None)
            .await
            .map_err(ReconcilerError::BuilderError)?;
        let gameserver = reconcile_builder(
            &self.gameserver_builder,
            minecraft_server.as_ref(),
            Some(GameServerBuilderContext {
                cluster: &cluster,
                agent_config: &self.agent_config,
            }),
        )
        .await
        .map_err(ReconcilerError::BuilderError)?;

        if let Some(gameserver) = &gameserver {
            if let Some(gameserver_status) = &gameserver.status {
                if gameserver_status.state == "Shutdown" {
                    api.delete(&minecraft_server.name_any(), &DeleteParams::default())
                        .await
                        .map_err(ReconcilerError::FailedToDeleteStale)?;

                    return Ok(Action::await_change());
                }

                let mut minecraft_server = minecraft_server.as_ref().clone();
                if minecraft_server.status.is_none() {
                    minecraft_server.status = Some(MinecraftServerStatus::default());
                }

                let status = minecraft_server.status.as_mut().unwrap();

                status.address = gameserver_status.address.clone();
                status.port = gameserver_status.ports.first().map(|p| p.port);

                if gameserver_status.state == "Ready" || gameserver_status.state == "Allocated" {
                    status.set_condition(
                        "Ready".to_string(),
                        ConditionStatus::True,
                        "ReadyOrAllocated".to_string(),
                        "Server is ready and maybe already allocated".to_string(),
                    );
                } else {
                    status.set_condition(
                        "Ready".to_string(),
                        ConditionStatus::False,
                        "Unknown".to_string(),
                        "Server is not ready yet".to_string(),
                    );
                };

                patch_status(
                    &api,
                    &PatchParams::apply("shulker-operator").force(),
                    &minecraft_server,
                )
                .await
                .map_err(ReconcilerError::BuilderError)?;
            }
        }

        Ok(Action::requeue(Duration::from_secs(5 * 60)))
    }

    async fn cleanup(&self, minecraft_server: Arc<MinecraftServer>) -> Result<Action> {
        info!(
            name = minecraft_server.name_any(),
            namespace = minecraft_server.namespace(),
            "cleaning up MinecraftServer",
        );

        Ok(Action::await_change())
    }

    fn get_labels(
        minecraft_server: &MinecraftServer,
        name: String,
        component: String,
    ) -> BTreeMap<String, String> {
        BTreeMap::from([
            ("app.kubernetes.io/name".to_string(), name.to_string()),
            (
                "app.kubernetes.io/instance".to_string(),
                format!("{}-{}", name, minecraft_server.name_any()),
            ),
            ("app.kubernetes.io/component".to_string(), component),
            (
                "app.kubernetes.io/part-of".to_string(),
                format!("cluster-{}", minecraft_server.spec.cluster_ref.name.clone()),
            ),
            (
                "app.kubernetes.io/managed-by".to_string(),
                "shulker-operator".to_string(),
            ),
            (
                "minecraftcluster.shulkermc.io/name".to_string(),
                minecraft_server.spec.cluster_ref.name.clone(),
            ),
        ])
    }
}

#[instrument(skip(ctx, minecraft_server))]
async fn reconcile(
    minecraft_server: Arc<MinecraftServer>,
    ctx: Arc<MinecraftServerReconciler>,
) -> Result<Action> {
    let ns = minecraft_server.namespace().unwrap();
    let minecraft_servers_api: Api<MinecraftServer> = Api::namespaced(ctx.client.clone(), &ns);

    info!(
        name = minecraft_server.name_any(),
        namespace = ns,
        "reconciling MinecraftServer",
    );

    if minecraft_server.metadata.deletion_timestamp.is_none() {
        ctx.reconcile(minecraft_servers_api.clone(), minecraft_server.clone())
            .await
    } else {
        ctx.cleanup(minecraft_server.clone()).await
    }
}

fn error_policy(
    _minecraft_server: Arc<MinecraftServer>,
    error: &ReconcilerError,
    _ctx: Arc<MinecraftServerReconciler>,
) -> Action {
    warn!("reconcile failed: {:?}", error);
    // ctx.metrics.reconcile_failure(&minecraft_server, error);
    Action::requeue(Duration::from_secs(5))
}

pub async fn run(client: Client, agent_config: AgentConfig) {
    let minecraft_servers_api = Api::<MinecraftServer>::all(client.clone());
    if let Err(e) = minecraft_servers_api
        .list(&ListParams::default().limit(1))
        .await
    {
        error!("CRD is not queryable; {e:?}. Is the CRD installed?");
        std::process::exit(1);
    }

    let context = MinecraftServerReconciler {
        client: client.clone(),
        agent_config,
        config_map_builder: ConfigMapBuilder::new(client.clone()),
        gameserver_builder: GameServerBuilder::new(client.clone()),
    };

    Controller::new(minecraft_servers_api, Config::default().any_semantic())
        .owns(
            Api::<ConfigMap>::all(client.clone()),
            Config::default().any_semantic(),
        )
        .owns(
            Api::<GameServer>::all(client.clone()),
            Config::default().any_semantic(),
        )
        .shutdown_on_signal()
        .run(reconcile, error_policy, context.into())
        .filter_map(|x| async move { std::result::Result::ok(x) })
        .for_each(|_| futures::future::ready(()))
        .await;
}

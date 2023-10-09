use std::{collections::BTreeMap, sync::Arc, time::Duration};

use futures::StreamExt;
use google_agones_crds::v1::game_server::GameServer;
use k8s_openapi::api::core::v1::ConfigMap;
use kube::{
    api::{ListParams, PatchParams},
    runtime::{
        controller::Action,
        finalizer::{finalizer, Event as Finalizer},
        watcher::Config,
        Controller,
    },
    Api, Client, ResourceExt,
};
use tracing::*;

use shulker_crds::{
    condition::{ConditionStatus, HasConditions},
    v1alpha1::minecraft_server_fleet::{MinecraftServerFleet, MinecraftServerFleetStatus},
};

use self::{
    config_map::ConfigMapBuilder, fleet::FleetBuilder, fleet_autoscaler::FleetAutoscalerBuilder,
};

use super::{builder::reconcile_builder, status::patch_status, ReconcilerError, Result};

mod config_map;
mod fleet;
mod fleet_autoscaler;

static FINALIZER: &str = "minecraftserverfleets.shulkermc.io";

struct MinecraftServerFleetReconciler {
    client: kube::Client,

    // Builders
    config_map_builder: ConfigMapBuilder,
    fleet_builder: FleetBuilder,
    fleet_autoscaler_builder: FleetAutoscalerBuilder,
}

impl MinecraftServerFleetReconciler {
    async fn reconcile(
        &self,
        api: Api<MinecraftServerFleet>,
        minecraft_server_fleet: Arc<MinecraftServerFleet>,
    ) -> Result<Action> {
        reconcile_builder(&self.config_map_builder, minecraft_server_fleet.as_ref()).await?;
        let fleet = reconcile_builder(&self.fleet_builder, minecraft_server_fleet.as_ref()).await?;
        reconcile_builder(
            &self.fleet_autoscaler_builder,
            minecraft_server_fleet.as_ref(),
        )
        .await?;

        if let Some(fleet) = &fleet {
            if let Some(fleet_status) = &fleet.status {
                let mut minecraft_server_fleet = minecraft_server_fleet.as_ref().clone();
                if minecraft_server_fleet.status.is_none() {
                    minecraft_server_fleet.status = Some(MinecraftServerFleetStatus::default());
                }

                let status = minecraft_server_fleet.status.as_mut().unwrap();

                status.replicas = fleet_status.replicas;
                status.ready_replicas = fleet_status.ready_replicas;
                status.allocated_replicas = fleet_status.allocated_replicas;

                if status.ready_replicas > 0 || status.allocated_replicas > 0 {
                    status.set_condition(
                        "Available".to_string(),
                        ConditionStatus::True,
                        "AtLeastOneReadyOrAllocated".to_string(),
                        "One or more servers are ready or allocated".to_string(),
                    );
                } else {
                    status.set_condition(
                        "Available".to_string(),
                        ConditionStatus::False,
                        "NoneReady".to_string(),
                        "No server are ready".to_string(),
                    );
                };

                patch_status(
                    &api,
                    &PatchParams::apply("shulker-operator"),
                    &minecraft_server_fleet,
                )
                .await?;
            }
        }

        Ok(Action::requeue(Duration::from_secs(5 * 60)))
    }

    async fn cleanup(&self, minecraft_server_fleet: Arc<MinecraftServerFleet>) -> Result<Action> {
        info!(
            name = minecraft_server_fleet.name_any(),
            namespace = minecraft_server_fleet.namespace(),
            "cleaning up MinecraftServerFleet",
        );

        Ok(Action::await_change())
    }

    fn get_common_labels(
        minecraft_server_fleet: &MinecraftServerFleet,
    ) -> BTreeMap<String, String> {
        BTreeMap::from([
            (
                "app.kubernetes.io/name".to_string(),
                minecraft_server_fleet.name_any(),
            ),
            (
                "app.kubernetes.io/component".to_string(),
                "minecraftserver".to_string(),
            ),
            (
                "minecraftcluster.shulkermc.io/name".to_string(),
                minecraft_server_fleet.spec.cluster_ref.name.clone(),
            ),
            (
                "minecraftserverfleet.shulkermc.io/name".to_string(),
                minecraft_server_fleet.name_any(),
            ),
        ])
    }
}

#[instrument(skip(ctx, minecraft_server_fleet))]
async fn reconcile(
    minecraft_server_fleet: Arc<MinecraftServerFleet>,
    ctx: Arc<MinecraftServerFleetReconciler>,
) -> Result<Action> {
    let ns = minecraft_server_fleet.namespace().unwrap();
    let minecraft_server_fleets_api: Api<MinecraftServerFleet> =
        Api::namespaced(ctx.client.clone(), &ns);

    info!(
        name = minecraft_server_fleet.name_any(),
        namespace = ns,
        "reconciling MinecraftServerFleet",
    );

    finalizer(
        &minecraft_server_fleets_api,
        FINALIZER,
        minecraft_server_fleet,
        |event| async {
            match event {
                Finalizer::Apply(minecraft_server_fleet) => {
                    ctx.reconcile(
                        minecraft_server_fleets_api.clone(),
                        minecraft_server_fleet.clone(),
                    )
                    .await
                }
                Finalizer::Cleanup(minecraft_server_fleet) => {
                    ctx.cleanup(minecraft_server_fleet.clone()).await
                }
            }
        },
    )
    .await
    .map_err(|e| ReconcilerError::FinalizerError(Box::new(e)))
}

fn error_policy(
    _minecraft_server_fleet: Arc<MinecraftServerFleet>,
    error: &ReconcilerError,
    _ctx: Arc<MinecraftServerFleetReconciler>,
) -> Action {
    warn!("reconcile failed: {:?}", error);
    // ctx.metrics.reconcile_failure(&minecraft_server_fleet, error);
    Action::requeue(Duration::from_secs(5))
}

pub async fn run(client: Client) {
    let minecraft_server_fleets_api = Api::<MinecraftServerFleet>::all(client.clone());
    if let Err(e) = minecraft_server_fleets_api
        .list(&ListParams::default().limit(1))
        .await
    {
        error!("CRD is not queryable; {e:?}. Is the CRD installed?");
        std::process::exit(1);
    }

    let context = MinecraftServerFleetReconciler {
        client: client.clone(),
        config_map_builder: ConfigMapBuilder::new(client.clone()),
        fleet_builder: FleetBuilder::new(client.clone()),
        fleet_autoscaler_builder: FleetAutoscalerBuilder::new(client.clone()),
    };

    Controller::new(
        minecraft_server_fleets_api,
        Config::default().any_semantic(),
    )
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

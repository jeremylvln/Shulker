use std::{collections::BTreeMap, sync::Arc, time::Duration};

use futures::StreamExt;
use k8s_openapi::api::core::v1::{ConfigMap, Service};
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
    agones::{fleet::Fleet, fleet_autoscaler::FleetAutoscaler},
    condition::{ConditionStatus, HasConditions},
    v1alpha1::proxy_fleet::{ProxyFleet, ProxyFleetStatus},
};

use self::{
    config_map::ConfigMapBuilder, fleet::FleetBuilder, fleet_autoscaler::FleetAutoscalerBuilder,
    service::ServiceBuilder,
};

use super::{builder::reconcile_builder, status::patch_status, ReconcilerError, Result};

mod config_map;
mod fleet;
mod fleet_autoscaler;
mod service;

static FINALIZER: &str = "proxyfleets.shulkermc.io";

struct ProxyFleetReconciler {
    client: kube::Client,

    // Builders
    config_map_builder: ConfigMapBuilder,
    service_builder: ServiceBuilder,
    fleet_builder: FleetBuilder,
    fleet_autoscaler_builder: FleetAutoscalerBuilder,
}

impl ProxyFleetReconciler {
    async fn reconcile(
        &self,
        api: Api<ProxyFleet>,
        proxy_fleet: Arc<ProxyFleet>,
    ) -> Result<Action> {
        reconcile_builder(&self.config_map_builder, proxy_fleet.as_ref()).await?;
        reconcile_builder(&self.service_builder, proxy_fleet.as_ref()).await?;
        let fleet = reconcile_builder(&self.fleet_builder, proxy_fleet.as_ref()).await?;
        reconcile_builder(&self.fleet_autoscaler_builder, proxy_fleet.as_ref()).await?;

        if let Some(fleet) = &fleet {
            if let Some(fleet_status) = &fleet.status {
                let mut proxy_fleet = proxy_fleet.as_ref().clone();
                if proxy_fleet.status.is_none() {
                    proxy_fleet.status = Some(ProxyFleetStatus::default());
                }

                let status = proxy_fleet.status.as_mut().unwrap();

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

                patch_status(&api, &PatchParams::apply("shulker-operator"), &proxy_fleet).await?;
            }
        }

        Ok(Action::requeue(Duration::from_secs(5 * 60)))
    }

    async fn cleanup(&self, proxy_fleet: Arc<ProxyFleet>) -> Result<Action> {
        info!(
            name = proxy_fleet.name_any(),
            namespace = proxy_fleet.namespace(),
            "cleaning up ProxyFleet",
        );

        Ok(Action::await_change())
    }

    fn get_common_labels(proxy_fleet: &ProxyFleet) -> BTreeMap<String, String> {
        BTreeMap::from([
            ("app.kubernetes.io/name".to_string(), proxy_fleet.name_any()),
            (
                "app.kubernetes.io/component".to_string(),
                "proxy".to_string(),
            ),
            (
                "minecraftcluster.shulkermc.io/name".to_string(),
                proxy_fleet.spec.cluster_ref.name.clone(),
            ),
            (
                "proxyfleet.shulkermc.io/name".to_string(),
                proxy_fleet.name_any(),
            ),
        ])
    }
}

#[instrument(skip(ctx, proxy_fleet))]
async fn reconcile(proxy_fleet: Arc<ProxyFleet>, ctx: Arc<ProxyFleetReconciler>) -> Result<Action> {
    let ns = proxy_fleet.namespace().unwrap();
    let proxy_fleets_api: Api<ProxyFleet> = Api::namespaced(ctx.client.clone(), &ns);

    info!(
        name = proxy_fleet.name_any(),
        namespace = ns,
        "reconciling ProxyFleet",
    );

    finalizer(&proxy_fleets_api, FINALIZER, proxy_fleet, |event| async {
        match event {
            Finalizer::Apply(proxy_fleet) => {
                ctx.reconcile(proxy_fleets_api.clone(), proxy_fleet.clone())
                    .await
            }
            Finalizer::Cleanup(proxy_fleet) => ctx.cleanup(proxy_fleet.clone()).await,
        }
    })
    .await
    .map_err(|e| ReconcilerError::FinalizerError(Box::new(e)))
}

fn error_policy(
    _proxy_fleet: Arc<ProxyFleet>,
    error: &ReconcilerError,
    _ctx: Arc<ProxyFleetReconciler>,
) -> Action {
    warn!("reconcile failed: {:?}", error);
    // ctx.metrics.reconcile_failure(&proxy_fleet, error);
    Action::requeue(Duration::from_secs(5))
}

pub async fn run(client: Client) {
    let proxy_fleets_api = Api::<ProxyFleet>::all(client.clone());
    if let Err(e) = proxy_fleets_api.list(&ListParams::default().limit(1)).await {
        error!("CRD is not queryable; {e:?}. Is the CRD installed?");
        std::process::exit(1);
    }

    let context = ProxyFleetReconciler {
        client: client.clone(),
        config_map_builder: ConfigMapBuilder::new(client.clone()),
        service_builder: ServiceBuilder::new(client.clone()),
        fleet_builder: FleetBuilder::new(client.clone()),
        fleet_autoscaler_builder: FleetAutoscalerBuilder::new(client.clone()),
    };

    Controller::new(proxy_fleets_api, Config::default().any_semantic())
        .owns(
            Api::<ConfigMap>::all(client.clone()),
            Config::default().any_semantic(),
        )
        .owns(
            Api::<Service>::all(client.clone()),
            Config::default().any_semantic(),
        )
        .owns(
            Api::<Fleet>::all(client.clone()),
            Config::default().any_semantic(),
        )
        .owns(
            Api::<FleetAutoscaler>::all(client.clone()),
            Config::default().any_semantic(),
        )
        .shutdown_on_signal()
        .run(reconcile, error_policy, context.into())
        .filter_map(|x| async move { std::result::Result::ok(x) })
        .for_each(|_| futures::future::ready(()))
        .await;
}

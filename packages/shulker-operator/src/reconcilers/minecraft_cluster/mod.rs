use std::{collections::BTreeMap, sync::Arc, time::Duration};

use futures::StreamExt;
use k8s_openapi::api::{
    core::v1::{Secret, ServiceAccount},
    rbac::v1::{Role, RoleBinding},
};
use kube::{
    api::ListParams,
    runtime::{
        controller::Action,
        finalizer::{finalizer, Event as Finalizer},
        watcher::Config,
        Controller,
    },
    Api, Client, ResourceExt,
};
use tracing::*;

use shulker_crds::v1alpha1::minecraft_cluster::MinecraftCluster;

use self::{
    forwarding_secret::ForwardingSecretBuilder, minecraft_server_role::MinecraftServerRoleBuilder,
    minecraft_server_role_binding::MinecraftServerRoleBindingBuilder,
    minecraft_server_service_account::MinecraftServerServiceAccountBuilder,
    proxy_role::ProxyRoleBuilder, proxy_role_binding::ProxyRoleBindingBuilder,
    proxy_service_account::ProxyServiceAccountBuilder,
};

use super::{builder::reconcile_builder, ReconcilerError, Result};

mod forwarding_secret;
mod minecraft_server_role;
mod minecraft_server_role_binding;
mod minecraft_server_service_account;
mod proxy_role;
mod proxy_role_binding;
mod proxy_service_account;

static FINALIZER: &str = "minecraftclusters.shulkermc.io";

struct MinecraftClusterReconciler {
    client: kube::Client,

    // Builders
    forwarding_secret_builder: ForwardingSecretBuilder,
    proxy_service_account_builder: ProxyServiceAccountBuilder,
    proxy_role_builder: ProxyRoleBuilder,
    proxy_role_binding_builder: ProxyRoleBindingBuilder,
    minecraft_server_service_account_builder: MinecraftServerServiceAccountBuilder,
    minecraft_server_role_builder: MinecraftServerRoleBuilder,
    minecraft_server_role_binding_builder: MinecraftServerRoleBindingBuilder,
}

impl MinecraftClusterReconciler {
    async fn reconcile(
        &self,
        _api: Api<MinecraftCluster>,
        cluster: Arc<MinecraftCluster>,
    ) -> Result<Action> {
        reconcile_builder(&self.forwarding_secret_builder, cluster.as_ref()).await?;
        reconcile_builder(&self.proxy_service_account_builder, cluster.as_ref()).await?;
        reconcile_builder(&self.proxy_role_builder, cluster.as_ref()).await?;
        reconcile_builder(&self.proxy_role_binding_builder, cluster.as_ref()).await?;
        reconcile_builder(
            &self.minecraft_server_service_account_builder,
            cluster.as_ref(),
        )
        .await?;
        reconcile_builder(&self.minecraft_server_role_builder, cluster.as_ref()).await?;
        reconcile_builder(
            &self.minecraft_server_role_binding_builder,
            cluster.as_ref(),
        )
        .await?;

        Ok(Action::requeue(Duration::from_secs(5 * 60)))
    }

    async fn cleanup(&self, cluster: Arc<MinecraftCluster>) -> Result<Action> {
        info!(
            name = cluster.name_any(),
            namespace = cluster.namespace(),
            "cleaning up MinecraftCluster",
        );

        Ok(Action::await_change())
    }

    fn get_common_labels(cluster: &MinecraftCluster) -> BTreeMap<String, String> {
        BTreeMap::from([
            ("app.kubernetes.io/name".to_string(), cluster.name_any()),
            (
                "app.kubernetes.io/component".to_string(),
                "cluster".to_string(),
            ),
            (
                "minecraftcluster.shulkermc.io/name".to_string(),
                cluster.name_any(),
            ),
        ])
    }
}

#[instrument(skip(ctx, cluster))]
async fn reconcile(
    cluster: Arc<MinecraftCluster>,
    ctx: Arc<MinecraftClusterReconciler>,
) -> Result<Action> {
    let ns = cluster.namespace().unwrap();
    let clusters_api: Api<MinecraftCluster> = Api::namespaced(ctx.client.clone(), &ns);

    info!(
        name = cluster.name_any(),
        namespace = ns,
        "reconciling MinecraftCluster",
    );

    finalizer(&clusters_api, FINALIZER, cluster, |event| async {
        match event {
            Finalizer::Apply(cluster) => ctx.reconcile(clusters_api.clone(), cluster.clone()).await,
            Finalizer::Cleanup(cluster) => ctx.cleanup(cluster.clone()).await,
        }
    })
    .await
    .map_err(|e| ReconcilerError::FinalizerError(Box::new(e)))
}

fn error_policy(
    _cluster: Arc<MinecraftCluster>,
    error: &ReconcilerError,
    _ctx: Arc<MinecraftClusterReconciler>,
) -> Action {
    warn!("reconcile failed: {:?}", error);
    // ctx.metrics.reconcile_failure(&cluster, error);
    Action::requeue(Duration::from_secs(5))
}

pub async fn run(client: Client) {
    let clusters_api = Api::<MinecraftCluster>::all(client.clone());
    if let Err(e) = clusters_api.list(&ListParams::default().limit(1)).await {
        error!("CRD is not queryable; {e:?}. Is the CRD installed?");
        std::process::exit(1);
    }

    let context = MinecraftClusterReconciler {
        client: client.clone(),
        forwarding_secret_builder: ForwardingSecretBuilder::new(client.clone()),
        proxy_service_account_builder: ProxyServiceAccountBuilder::new(client.clone()),
        proxy_role_builder: ProxyRoleBuilder::new(client.clone()),
        proxy_role_binding_builder: ProxyRoleBindingBuilder::new(client.clone()),
        minecraft_server_service_account_builder: MinecraftServerServiceAccountBuilder::new(
            client.clone(),
        ),
        minecraft_server_role_builder: MinecraftServerRoleBuilder::new(client.clone()),
        minecraft_server_role_binding_builder: MinecraftServerRoleBindingBuilder::new(
            client.clone(),
        ),
    };

    Controller::new(clusters_api, Config::default().any_semantic())
        .owns(
            Api::<Secret>::all(client.clone()),
            Config::default().any_semantic(),
        )
        .owns(
            Api::<ServiceAccount>::all(client.clone()),
            Config::default().any_semantic(),
        )
        .owns(
            Api::<Role>::all(client.clone()),
            Config::default().any_semantic(),
        )
        .owns(
            Api::<RoleBinding>::all(client.clone()),
            Config::default().any_semantic(),
        )
        .shutdown_on_signal()
        .run(reconcile, error_policy, context.into())
        .filter_map(|x| async move { std::result::Result::ok(x) })
        .for_each(|_| futures::future::ready(()))
        .await;
}

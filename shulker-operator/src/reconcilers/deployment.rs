use futures::{future::BoxFuture, FutureExt, StreamExt};
use k8s_openapi::api::apps::v1::Deployment;
use kube::{
    api::{Api, DeleteParams, ListParams, Meta},
    client::Client,
};
use kube_runtime::controller::{Context, Controller, ReconcilerAction};
use snafu::{ResultExt, Snafu};
use tokio::time::Duration;
use tracing::{debug, error, info, instrument};

use shulker_crds::minecraft_server::*;

/// Enumeration of possible errors concerning the
/// reconciliation of Deployment resources.
#[derive(Debug, Snafu)]
pub enum Error {
    /// The Deployment resource is malformed (maybe
    /// because it has been modified by someone else).
    #[snafu(display("Deployment {} is malformed: {}", deployment, reason))]
    DeploymentMalformed { deployment: String, reason: String },
    /// Kubernetes's API rejected the deletion of an
    /// existing Deployment.
    #[snafu(display("Failed to delete a deployment: {}", source))]
    DeploymentDeletionFailed { source: kube::Error },
}

/// Context structure provided when reconciling
/// a resource.
#[derive(Clone)]
struct Data {
    /// Kubernetes client.
    client: Client,
}

/// Deployment resource reconciler.
///
/// It only job is to ensure that the deployment
/// is linked to a MinecraftServer resource. If
/// the operator boots in an environment where
/// orphan deployments are found, they will be
/// deleted.
///
/// # Arguments
/// - `deployment` - Deployment resource
/// - `ctx` - Context
#[instrument(skip(deployment, ctx))]
async fn reconcile(deployment: Deployment, ctx: Context<Data>) -> Result<ReconcilerAction, Error> {
    let client = ctx.get_ref().client.clone();
    let name = Meta::name(&deployment);
    let ns = Meta::namespace(&deployment).expect("Deployment is namespaced");
    let labels = Meta::meta(&deployment).labels.as_ref().unwrap();
    debug!("reconcile Deployment {}/{}", ns, name);
    let mcs: Api<MinecraftServer> = Api::namespaced(client.clone(), &ns);

    if !labels.contains_key("shulker.io/minecraft-server-name") {
        return Err(Error::DeploymentMalformed {
            deployment: name,
            reason: "MinecraftServer label not found".to_owned(),
        });
    }

    match mcs
        .get(labels.get("shulker.io/minecraft-server-name").unwrap())
        .await
    {
        Ok(_) => Ok(ReconcilerAction {
            requeue_after: None,
        }),
        Err(e) => match e {
            kube::Error::Api(kube::error::ErrorResponse { code: 404, .. }) => {
                let deployments: Api<Deployment> = Api::namespaced(client.clone(), &ns);

                deployments
                    .delete(&name, &DeleteParams::default())
                    .await
                    .context(DeploymentDeletionFailed)?;

                Ok(ReconcilerAction {
                    requeue_after: None,
                })
            }
            _ => Err(Error::DeploymentDeletionFailed { source: e }),
        },
    }
}

/// Error policy to call when a Deployment
/// reconciliation fails.
///
/// # Arguments
/// - `error` - Occured error
/// - `_ctx` - Context
fn error_policy(error: &Error, _ctx: Context<Data>) -> ReconcilerAction {
    error!("reconcile failed for Deployment: {}", error);
    ReconcilerAction {
        requeue_after: Some(Duration::from_secs(60)),
    }
}

/// Create a controller for Deployment
/// resources.
///
/// # Arguments
/// - `client` - Kubernetes client
pub fn drainer(client: Client) -> BoxFuture<'static, ()> {
    let context = Context::new(Data {
        client: client.clone(),
    });
    let resources: Api<Deployment> = Api::all(client);
    let lp = ListParams::default().labels("shulker.io/managed-by=shulker");

    info!("starting reconciliation for Deployment resources");
    Controller::new(resources, lp)
        .run(reconcile, error_policy, context)
        .filter_map(|x| async move { std::result::Result::ok(x) })
        .for_each(|o| {
            let ns = o.0.namespace.unwrap();
            info!("reconciled Deployment: {}/{}", ns, o.0.name);
            futures::future::ready(())
        })
        .boxed()
}

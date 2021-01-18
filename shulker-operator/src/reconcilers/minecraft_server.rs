use chrono::Utc;
use futures::{future::BoxFuture, FutureExt, StreamExt};
use kube::{
    api::{Api, ListParams, Meta, PatchParams},
    client::Client,
};
use kube_runtime::controller::{Context, Controller, ReconcilerAction};
use serde_json::json;
use snafu::{ResultExt, Snafu};
use tokio::time::Duration;
use tracing::{debug, error, info, instrument};

use shulker_crds::minecraft_server::*;
use shulker_instance::minecraft_server::deployment;

use crate::templates::get_template;

#[derive(Debug, Snafu)]
pub enum Error {
    #[snafu(display("Template {} not found: {}", template, source))]
    MinecraftServerTemplateNotFound {
        template: String,
        source: kube::Error,
    },
    #[snafu(display("Failed to deploy MinecraftServer: {}", source))]
    MinecraftServerDeploymentFailed {
        source: shulker_instance::minecraft_server::deployment::Error,
    },
    #[snafu(display("Failed to patch MinecraftServer: {}", source))]
    MinecraftServerPatchFailed {
        source: kube::Error,
    },
    SerializationFailed {
        source: serde_json::Error,
    },
}

#[derive(Clone)]
struct Data {
    client: Client,
}

#[instrument(skip(mc, ctx))]
async fn reconcile(mc: MinecraftServer, ctx: Context<Data>) -> Result<ReconcilerAction, Error> {
    let client = ctx.get_ref().client.clone();
    let name = Meta::name(&mc);
    let ns = Meta::namespace(&mc).expect("MinecraftServer is namespaced");
    debug!("reconcile MinecraftServer {}/{}: {:?}", ns, name, mc);
    let mcs: Api<MinecraftServer> = Api::namespaced(client.clone(), &ns);

    let template = get_template(client.clone(), &mc.spec.template, &ns)
        .await
        .context(MinecraftServerTemplateNotFound {
            template: mc.spec.template.clone(),
        })?;

    if !template.spec.schedulable.unwrap_or(false) {
        error!(
            "primary template of MinecraftServer {}/{} is not schedulable",
            ns, name
        );
        return Ok(ReconcilerAction {
            requeue_after: Some(Duration::from_secs(60 * 5)),
        });
    }

    deployment::ensure_deployment(client.clone(), &name, &name, &ns, &template)
        .await
        .context(MinecraftServerDeploymentFailed)?;

    let new_status = serde_json::to_vec(&json!({
        "status": MinecraftServerStatus {
            conditions: vec![
                MinecraftServerStatusCondition {
                    last_transition_time: Utc::now(),
                    message: Some("Deployment synced with template".to_owned()),
                    reason: Some("Deployment synced with template".to_owned()),
                    status: "True".to_owned(),
                    r#type: "Ready".to_owned(),
                }
            ],
            players: 0
        }
    }))
    .context(SerializationFailed)?;

    mcs.patch_status(&name, &PatchParams::default(), new_status)
        .await
        .context(MinecraftServerPatchFailed)?;

    Ok(ReconcilerAction {
        requeue_after: Some(Duration::from_secs(60 * 5)),
    })
}

fn error_policy(error: &Error, _ctx: Context<Data>) -> ReconcilerAction {
    error!("reconcile failed for MinecraftServer: {}", error);
    ReconcilerAction {
        requeue_after: Some(Duration::from_secs(60)),
    }
}

pub fn drainer(client: Client) -> BoxFuture<'static, ()> {
    let context = Context::new(Data {
        client: client.clone(),
    });
    let resources = Api::<MinecraftServer>::all(client);

    info!("starting reconciliation for MinecraftServer resources");
    Controller::new(resources, ListParams::default())
        .run(reconcile, error_policy, context)
        .filter_map(|x| async move { std::result::Result::ok(x) })
        .for_each(|o| {
            let ns = o.0.namespace.unwrap();
            info!("reconciled MinecraftServer: {}/{}", ns, o.0.name);
            futures::future::ready(())
        })
        .boxed()
}

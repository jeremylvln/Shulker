use futures::{future::BoxFuture, FutureExt, StreamExt};
use kube::{
    api::{Api, ListParams, Meta, PatchParams},
    client::Client,
};
use kube_runtime::controller::{Context, Controller, ReconcilerAction};
use serde_json::json;
use snafu::{ResultExt, Snafu};
use std::sync::{Arc, RwLock};
use tokio::time::Duration;
use tracing::{debug, error, info, instrument};

use crate::templates::compose::fold_template_spec;
use shulker_crds::minecraft_server_template::*;
use shulker_resource::storage::ResourceStorage;

#[derive(Debug, Snafu)]
pub enum Error {
    #[snafu(display("Failed to aggregate MinecraftServerTemplate: {}", source))]
    MinecraftServerTemplateAggregationFailed {
        source: kube::Error,
    },
    #[snafu(display("Failed to patch MinecraftServerTemplate: {}", source))]
    MinecraftServerTemplatePatchFailed {
        source: kube::Error,
    },
    SerializationFailed {
        source: serde_json::Error,
    },
}

#[derive(Clone)]
struct Data {
    client: Client,
    resource_storage: Arc<RwLock<ResourceStorage>>,
}

#[instrument(skip(mct, ctx))]
async fn reconcile(
    mct: MinecraftServerTemplate,
    ctx: Context<Data>,
) -> Result<ReconcilerAction, Error> {
    let client = ctx.get_ref().client.clone();
    let name = Meta::name(&mct);
    let ns = Meta::namespace(&mct).expect("MinecraftServerTemplate is namespaced");

    debug!(
        "reconcile MinecraftServerTemplate {}/{}: {:?}",
        ns, name, mct
    );
    let mcs: Api<MinecraftServerTemplate> = Api::namespaced(client.clone(), &ns);

    let composed = fold_template_spec(client.clone(), &mct)
        .await
        .context(MinecraftServerTemplateAggregationFailed)?;

    if let Some(assets) = composed.assets.as_ref() {
        let mut resource_storage = ctx.get_ref().resource_storage.write().unwrap();

        if let Some(maps) = assets.maps.as_ref() {
            for map in maps {
                resource_storage
                    .push(&map.name, &map.provider, &map.spec)
                    .await;
            }
        }
        if let Some(plugins) = assets.plugins.as_ref() {
            for plugin in plugins {
                resource_storage
                    .push(&plugin.name, &plugin.provider, &plugin.spec)
                    .await;
            }
        }
    }

    let new_status = serde_json::to_vec(&json!({
        "status": MinecraftServerTemplateStatus {
            instances: 0,
            players: 0,
            compose_result: serde_json::to_string(&composed).context(SerializationFailed)?,
        }
    }))
    .context(SerializationFailed)?;

    mcs.patch_status(&name, &PatchParams::default(), new_status)
        .await
        .context(MinecraftServerTemplatePatchFailed)?;

    Ok(ReconcilerAction {
        requeue_after: Some(Duration::from_secs(60 * 30)),
    })
}

fn error_policy(error: &Error, _ctx: Context<Data>) -> ReconcilerAction {
    error!("reconcile failed for MinecraftServerTemplate: {}", error);
    ReconcilerAction {
        requeue_after: Some(Duration::from_secs(60)),
    }
}

pub fn drainer(
    client: Client,
    resource_storage: Arc<RwLock<ResourceStorage>>,
) -> BoxFuture<'static, ()> {
    let context = Context::new(Data {
        client: client.clone(),
        resource_storage: resource_storage.clone(),
    });
    let resources: Api<MinecraftServerTemplate> = Api::all(client);

    info!("starting reconciliation for MinecraftServerTemplate resources");
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

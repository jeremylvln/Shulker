use futures::{future::BoxFuture, FutureExt, StreamExt};
use kube::{
    api::{Api, ListParams, Meta, Patch, PatchParams},
    client::Client,
};
use kube_runtime::controller::{Context, Controller, ReconcilerAction};
use serde_json::json;
use snafu::{ResultExt, Snafu};
use std::sync::Arc;
use tokio::{sync::RwLock, time::Duration};
use tracing::{debug, error, info, instrument};

use crate::templates::compose::fold_template_spec;
use shulker_crds::minecraft_server_template::*;
use shulker_resource::storage::ResourceStorage;

/// Enumeration of possible errors concerning the
/// reconciliation of MinecraftServerTemplate resources.
#[derive(Debug, Snafu)]
pub enum Error {
    /// Failed to fold a MinecraftServerTemplate
    /// resource. 
    #[snafu(display("Failed to fold MinecraftServerTemplate: {}", source))]
    MinecraftServerTemplateFoldFailed {
        source: kube::Error,
    },
    /// Kubernetes's API rejected the patch of an
    /// existing MinecraftServerTemplate path.
    #[snafu(display("Failed to patch MinecraftServerTemplate: {}", source))]
    MinecraftServerTemplatePatchFailed {
        source: kube::Error,
    },
    /// Something went wrong when serializing from or
    /// deserializing to JSON.
    SerializationFailed {
        source: serde_json::Error,
    },
}

/// Context structure provided when reconciling
/// a resource.
#[derive(Clone)]
struct Data {
    /// Kubernetes client.
    client: Client,
    /// Resource storage instance.
    resource_storage: Arc<RwLock<ResourceStorage>>,
}

/// MinecraftServerTemplate resource reconciler.
/// 
/// It will fold each templates and will register all
/// the resources listed (and fetch them if needed).
/// 
/// # Arguments
/// - `deployment` - MinecraftServerTemplate resource
/// - `ctx` - Context
#[instrument(skip(mct, ctx))]
async fn reconcile(
    mct: MinecraftServerTemplate,
    ctx: Context<Data>,
) -> Result<ReconcilerAction, Error> {
    let client = ctx.get_ref().client.clone();
    let name = Meta::name(&mct);
    let ns = Meta::namespace(&mct).expect("MinecraftServerTemplate is namespaced");

    debug!("reconcile MinecraftServerTemplate {}/{}", ns, name);
    let mcs: Api<MinecraftServerTemplate> = Api::namespaced(client.clone(), &ns);

    let composed = fold_template_spec(client.clone(), &mct)
        .await
        .context(MinecraftServerTemplateFoldFailed)?;

    if let Some(assets) = composed.assets.as_ref() {
        let mut rs = ctx.get_ref().resource_storage.write().await;

        let mut resources = Vec::new();
        if let Some(maps) = assets.maps.as_ref() {
            resources.extend(maps.iter());
        }
        if let Some(plugins) = assets.plugins.as_ref() {
            resources.extend(plugins.iter());
        }

        // @todo Wait the resources in parallel
        for resource in resources {
            let proxy = rs
                .create_proxy(&resource.name, &resource.provider, &resource.spec)
                .await
                .expect("aaa");
            proxy.write().await.fetch().await.expect("bbb");
        }
    }

    let new_status = json!({
        "status": MinecraftServerTemplateStatus {
            compose_result: serde_json::to_string(&composed).context(SerializationFailed)?,
        }
    });

    mcs.patch_status(&name, &PatchParams::default(), &Patch::Merge(&new_status))
        .await
        .context(MinecraftServerTemplatePatchFailed)?;

    Ok(ReconcilerAction {
        requeue_after: Some(Duration::from_secs(60 * 30)),
    })
}

/// Error policy to call when a MinecraftServerTemplate
/// reconciliation fails.
/// 
/// # Arguments
/// - `error` - Occured error
/// - `_ctx` - Context
fn error_policy(error: &Error, _ctx: Context<Data>) -> ReconcilerAction {
    error!("reconcile failed for MinecraftServerTemplate: {}", error);
    ReconcilerAction {
        requeue_after: Some(Duration::from_secs(60)),
    }
}

/// Create a controller for MinecraftServerTemplate
/// resources.
/// 
/// # Arguments
/// - `client` - Kubernetes client
pub fn drainer(
    client: Client,
    resource_storage: Arc<RwLock<ResourceStorage>>,
) -> BoxFuture<'static, ()> {
    let context = Context::new(Data {
        client: client.clone(),
        resource_storage,
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

use envconfig::Envconfig;
use shulker_resource::storage::ResourceStorage;
use std::error::Error;
use tracing::{info, warn};

use serde_json::json;

mod config;
mod reconcilers;
mod templates;

const VERSION: &str = env!("CARGO_PKG_VERSION");

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    tracing_subscriber::fmt()
        .with_max_level(tracing::Level::INFO)
        .init();

    info!("    ########");
    info!("#####      ##");
    info!("##          ##     Shulker");
    info!("###   #########    Version {}", VERSION);
    info!("# #####       #");
    info!("#  ##         #");
    info!("   ##");

    let c = config::Config::init_from_env().unwrap();
    let mut rc = ResourceStorage::new(&c.cache_dir);
    rc.push(
        "test",
        "url",
        &json!({
            "url": "https://i.jeremylvln.fr/skyconqueror.zip"
        }),
    )
    .expect("ERR");

    rc.sync().await.expect(":(");

    let client = kube::Client::try_default()
        .await
        .expect("Failed to create Kubernetes client");

    shulker_crds::assert_installed_crds(client.clone())
        .await
        .expect("Missing Shulker's Kubernetes CRDs");

    tokio::select! {
        _ = reconcilers::deployment::drainer(client.clone()) => warn!("Deployment controller drained"),
        _ = reconcilers::minecraft_server_template::drainer(client.clone()) => warn!("MinecraftServerTemplate controller drained"),
        _ = reconcilers::minecraft_server::drainer(c.clone(), client.clone()) => warn!("MinecraftServer controller drained"),
    }

    Ok(())
}

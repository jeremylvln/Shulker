use std::error::Error;
use tracing::{info, warn};

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

    let client = kube::Client::try_default()
        .await
        .expect("Failed to create Kubernetes client");

    shulker_crds::assert_installed_crds(client.clone())
        .await
        .expect("Missing Shulker's Kubernetes CRDs");

    tokio::select! {
        _ = reconcilers::minecraft_server_template::drainer(client.clone()) => warn!("MinecraftServerTemplate controller drained"),
        _ = reconcilers::minecraft_server::drainer(client.clone()) => warn!("MinecraftServer controller drained"),
    }

    Ok(())
}

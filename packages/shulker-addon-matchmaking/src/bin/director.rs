use std::sync::{Arc, Mutex};

use clap::Parser;
use google_open_match_sdk::backend_service_client::BackendServiceClient;
use kube::Client;
use shulker_addon_matchmaking::{
    director, mmf::registry::MMFRegistry, queue_registry::QueueRegistry, reconcilers,
};
use shulker_crds::matchmaking::v1alpha1::matchmaking_queue::MatchmakingQueueMMFBuiltInType;
use shulker_kube_utils::{lease, metrics};
use shulker_sdk::minecraft_server_fleet_service_client::MinecraftServerFleetServiceClient;
use shulker_utils::telemetry;

const LEASE_NAME: &str = "shulker-addon-matchmaking.shulkermc.io";
const LEASE_CONTROLLER_NAME: &str = "shulker-addon-matchmaking";

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Args {
    /// The address the metrics HTTP server should bind to
    #[arg(long, default_value = "127.0.0.1:8080", value_name = "address")]
    metrics_bind_address: String,

    // The host of the backend service of Open Match
    #[arg(
        long,
        default_value = "open-match-backend.open-match",
        value_name = "host",
        env = "OPEN_MATCH_BACKEND_HOST"
    )]
    open_match_backend_host: String,

    // The port of the backend service of Open Match
    #[arg(
        long,
        default_value = "50505",
        value_name = "port",
        env = "OPEN_MATCH_BACKEND_GRPC_PORT"
    )]
    open_match_backend_grpc_port: u16,

    // The host of the API service of Shulker
    #[arg(
        long,
        default_value = "shulker-operator.shulker-system",
        value_name = "host",
        env = "SHULKER_API_HOST"
    )]
    shulker_api_host: String,

    // The port of the API service of Shulker
    #[arg(
        long,
        default_value = "8080",
        value_name = "port",
        env = "SHULKER_API_GRPC_PORT"
    )]
    shulker_api_grpc_port: u16,
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let args = Args::parse();

    telemetry::init().await;

    let cancellation_token = tokio_util::sync::CancellationToken::new();

    let client = Client::try_default().await?;
    let lease_holder = lease::try_acquire_and_hold(
        client.clone(),
        LEASE_NAME.to_string(),
        LEASE_CONTROLLER_NAME.to_string(),
        cancellation_token.clone(),
    )
    .await?;

    let mut mmf_registry = MMFRegistry::new();
    mmf_registry.register_mmf(
        MatchmakingQueueMMFBuiltInType::Batch,
        "shulker-addon-matchmaking-mmf.shulker-system".to_string(),
        9090,
    );
    mmf_registry.register_mmf(
        MatchmakingQueueMMFBuiltInType::Elo,
        "shulker-addon-matchmaking-mmf.shulker-system".to_string(),
        9091,
    );

    let queue_registry = Arc::new(Mutex::new(QueueRegistry::new(mmf_registry)));

    let director = director::run(
        client.clone(),
        BackendServiceClient::connect(format!(
            "http://{}:{}",
            args.open_match_backend_host, args.open_match_backend_grpc_port
        ))
        .await?,
        MinecraftServerFleetServiceClient::connect(format!(
            "http://{}:{}",
            args.shulker_api_host, args.shulker_api_grpc_port
        ))
        .await?,
        queue_registry.clone(),
        cancellation_token.clone(),
    )?;

    tokio::select! {
        _ = tokio::signal::ctrl_c() => {
            cancellation_token.cancel();
        },
        _ = lease_holder => {},
        _ = reconcilers::matchmaking_queue::run(client.clone(), queue_registry.clone()) => {},
        _ = director => {},
        _ = metrics::create_http_server(args.metrics_bind_address)? => {},
    }

    Ok(())
}

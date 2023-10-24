use clap::Parser;
use kube::Client;
use shulker_operator::{api, lease, reconcilers, telemetry};

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Args {
    /// The address the metrics HTTP server should bind to
    #[arg(long, default_value = "127.0.0.1:8080", value_name = "address")]
    metrics_bind_address: String,

    /// The address the API gRPC server should bind to
    #[arg(long, default_value = "0.0.0.0:9090", value_name = "address")]
    api_bind_address: String,
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let args = Args::parse();

    telemetry::init().await;

    let cancellation_token = tokio_util::sync::CancellationToken::new();

    let client = Client::try_default().await?;
    let lease_holder =
        lease::try_acquire_and_hold(client.clone(), cancellation_token.clone()).await?;

    tokio::select! {
        _ = tokio::signal::ctrl_c() => {
            cancellation_token.cancel();
        },
        _ = lease_holder => {},
        _ = reconcilers::minecraft_cluster::run(client.clone()) => {},
        _ = reconcilers::proxy_fleet::run(client.clone()) => {},
        _ = reconcilers::minecraft_server::run(client.clone()) => {},
        _ = reconcilers::minecraft_server_fleet::run(client.clone()) => {},
        _ = api::create_metrics_server(args.metrics_bind_address)? => {},
        _ = api::create_grpc_server(args.api_bind_address, client.clone()) => {},
    }

    Ok(())
}

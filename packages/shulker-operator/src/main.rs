use kube::Client;
use shulker_operator::{api, lease, reconcilers, telemetry};

#[tokio::main]
async fn main() -> anyhow::Result<()> {
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
        _ = api::create_http_server()? => {},
        _ = api::create_grpc_server(client.clone()) => {},
    }

    Ok(())
}

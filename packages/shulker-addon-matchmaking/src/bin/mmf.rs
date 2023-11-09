use std::net::ToSocketAddrs;

use clap::Parser;
use google_open_match_sdk::{
    match_function_server::MatchFunctionServer, query_service_client::QueryServiceClient,
};
use shulker_addon_matchmaking::mmf::batch::MatchFunctionBatch;
use shulker_kube_utils::metrics;
use shulker_utils::telemetry;
use tonic::transport::Server;

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Args {
    /// The address the metrics HTTP server should bind to
    #[arg(long, default_value = "127.0.0.1:8080", value_name = "address")]
    metrics_bind_address: String,

    // The host of the query service of Open Match
    #[arg(
        long,
        default_value = "open-match-query.open-match",
        value_name = "host",
        env = "OPEN_MATCH_QUERY_HOST"
    )]
    open_match_query_host: String,

    // The port of the query service of Open Match
    #[arg(
        long,
        default_value = "50503",
        value_name = "port",
        env = "OPEN_MATCH_QUERY_GRPC_PORT"
    )]
    open_match_query_grpc_port: u16,

    // The port of the built-in FIFO matchmaking function
    #[arg(
        long,
        default_value = "9090",
        value_name = "port",
        env = "MMF_BATCH_GRPC_PORT"
    )]
    mmf_batch_grpc_port: u16,

    // The port of the built-in ELO matchmaking function
    #[arg(
        long,
        default_value = "9091",
        value_name = "port",
        env = "MMF_ELO_GRPC_PORT"
    )]
    mmf_elo_grpc_port: u16,
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let args = Args::parse();

    telemetry::init().await;

    let query_client = QueryServiceClient::connect(format!(
        "http://{}:{}",
        args.open_match_query_host, args.open_match_query_grpc_port
    ))
    .await?;

    tokio::select! {
        _ = Server::builder().add_service(MatchFunctionServer::new(MatchFunctionBatch::new(query_client.clone()))).serve(format!("0.0.0.0:{}", args.mmf_batch_grpc_port).to_socket_addrs().unwrap().next().unwrap()) => {},
        _ = metrics::create_http_server(args.metrics_bind_address)? => {},
    }

    Ok(())
}

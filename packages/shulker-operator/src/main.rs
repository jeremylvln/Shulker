use clap::Parser;
use google_agones_sdk::allocation::allocation_service_client::AllocationServiceClient;
use http::Uri;
use kube::Client;
use shulker_kube_utils::{lease, metrics};
use shulker_operator::{
    agent::AgentConfig,
    api::{self, GrpcServerContext},
    constants, reconcilers,
};
use shulker_utils::telemetry;
use tonic::transport::{Channel, ClientTlsConfig, Identity};

const LEASE_NAME: &str = "shulker-operator.shulkermc.io";
const LEASE_CONTROLLER_NAME: &str = "shulker-operator";

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Args {
    /// The address the metrics HTTP server should bind to
    #[arg(long, default_value = "127.0.0.1:8080", value_name = "address")]
    metrics_bind_address: String,

    /// The address the API gRPC server should bind to
    #[arg(long, default_value = "0.0.0.0:9090", value_name = "address")]
    api_bind_address: String,

    // The host of the allocator service of Agones
    #[arg(
        long,
        default_value = "agones-allocator.agones-system",
        value_name = "host",
        env = "AGONES_ALLOCATOR_HOST"
    )]
    agones_allocator_host: String,

    // The port of the allocator service of Agones
    #[arg(
        long,
        default_value = "443",
        value_name = "port",
        env = "AGONES_ALLOCATOR_GRPC_PORT"
    )]
    agones_allocator_grpc_port: u16,

    /// The path to the mTLS certificate to use with the
    /// Agones Allocator service
    #[arg(long, value_name = "path")]
    agones_allocator_tls_client_crt: Option<String>,

    /// The path to the mTLS key to use with the Agones
    /// Allocator service
    #[arg(long, value_name = "path")]
    agones_allocator_tls_client_key: Option<String>,

    #[arg(
        long,
        default_value_t = constants::SHULKER_PLUGIN_REPOSITORY.to_string(),
        value_name = "url",
        env = "SHULKER_AGENT_MAVEN_REPOSITORY"
    )]
    agent_maven_repository: String,

    #[arg(
        long,
        default_value_t = constants::SHULKER_PLUGIN_VERSION.to_string(),
        value_name = "version",
        env = "SHULKER_AGENT_VERSION"
    )]
    agent_version: String,
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let args = Args::parse();

    telemetry::init().await;

    let client = Client::try_default().await?;
    let cancellation_token = tokio_util::sync::CancellationToken::new();

    let metrics_http_server = metrics::create_http_server(args.metrics_bind_address)?;
    let lease_holder = lease::try_acquire_and_hold(
        client.clone(),
        LEASE_NAME.to_string(),
        LEASE_CONTROLLER_NAME.to_string(),
        cancellation_token.clone(),
    )
    .await?;

    let mut agones_allocator_channel = Channel::builder(
        format!(
            "http://{}:{}",
            args.agones_allocator_host, args.agones_allocator_grpc_port
        )
        .parse::<Uri>()
        .unwrap(),
    );

    if let (Some(crt_path), Some(key_path)) = (
        args.agones_allocator_tls_client_crt,
        args.agones_allocator_tls_client_key,
    ) {
        agones_allocator_channel =
            agones_allocator_channel.tls_config(ClientTlsConfig::new().identity(
                Identity::from_pem(std::fs::read(crt_path)?, std::fs::read(key_path)?),
            ))?;
    }

    let agones_allocator_client =
        AllocationServiceClient::connect(agones_allocator_channel).await?;

    let agent_config = AgentConfig {
        maven_repository: args.agent_maven_repository,
        version: args.agent_version,
    };

    tokio::select! {
        _ = tokio::signal::ctrl_c() => {
            cancellation_token.cancel();
        },
        _ = metrics_http_server => {},
        _ = lease_holder => {},
        _ = reconcilers::minecraft_cluster::run(client.clone()) => {},
        _ = reconcilers::proxy_fleet::run(client.clone(), agent_config.clone()) => {},
        _ = reconcilers::minecraft_server::run(client.clone(), agent_config.clone()) => {},
        _ = reconcilers::minecraft_server_fleet::run(client.clone(), agent_config.clone()) => {},
        _ = api::create_grpc_server(args.api_bind_address, GrpcServerContext {
            client: client.clone(),
            agones_allocator_client,
        }) => {},
    }

    Ok(())
}

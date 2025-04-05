use clap::{arg, ArgMatches, Command};

use kube::Client;
use shulker_sdk::sdk_service_client::SdkServiceClient;
use tonic::transport::Channel;
use tracing::{debug, info};

use shulker_cli::commands;
use shulker_cli::CliContext;
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::EnvFilter;
use tracing_subscriber::Registry;

fn configure_logger() {
    let logger = tracing_subscriber::fmt::layer().compact();

    let env_filter = EnvFilter::try_from_default_env()
        .or(EnvFilter::try_new("info"))
        .unwrap();

    let collector = Registry::default().with(logger).with(env_filter);
    tracing::subscriber::set_global_default(collector).unwrap();
}

fn create_command() -> Command {
    Command::new("shulker")
        .about("A CLI to manage Shulker clusters")
        .subcommand_required(true)
        .arg_required_else_help(true)
        .arg(arg!(-n --namespace <NAMESPACE> "Kubernetes namespace to look into").global(true))
        .arg(
            arg!(--sdkhost <SDK_HOST> "Shulker gRPC host")
                .default_value("127.0.0.1")
                .global(true),
        )
        .arg(
            arg!(--sdkport <SDK_PORT> "Shulker gRPC port")
                .default_value("9090")
                .global(true),
        )
        .subcommand(
            Command::new("fleets")
                .about("Manage fleets")
                .arg_required_else_help(true)
                .subcommand(
                    Command::new("summon")
                        .about("Summon a game server manually")
                        .arg_required_else_help(true)
                        .arg(arg!(<FLEET> "Fleet name")),
                ),
        )
}

async fn create_sdk_client(args: &ArgMatches) -> anyhow::Result<SdkServiceClient<Channel>> {
    let sdk_host = args.get_one::<String>("sdkhost").unwrap();
    let sdk_port = args.get_one::<String>("sdkport").unwrap();

    let client = SdkServiceClient::connect(format!("http://{sdk_host}:{sdk_port}")).await?;
    Ok(client)
}

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    configure_logger();

    debug!("parsing command arguments");
    let matches = create_command().get_matches();

    info!("creating Kubernetes client");
    let client = Client::try_default().await?;
    let default_namespace = client.default_namespace().to_owned();

    info!("creating SDK client");
    let sdk_client = create_sdk_client(&matches).await?;

    let mut cli_context = CliContext {
        kube_client: client,
        kube_namespace: matches
            .get_one::<String>("namespace")
            .map(|s| s.to_owned())
            .unwrap_or_else(|| default_namespace),
        sdk_client,
    };

    match matches.subcommand() {
        Some(("fleets", sub_matches)) => {
            let server_command = sub_matches.subcommand().unwrap();

            match server_command {
                ("summon", sub_matches) => {
                    commands::fleet_summon::run(&mut cli_context, sub_matches).await?;
                }
                _ => unreachable!(),
            }
        }
        _ => unreachable!(),
    }

    Ok(())
}

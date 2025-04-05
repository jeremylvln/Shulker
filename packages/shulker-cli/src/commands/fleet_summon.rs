use std::collections::HashMap;

use clap::ArgMatches;

use shulker_sdk::FleetAllocationRequest;
use tracing::info;

use crate::CliContext;

pub async fn run(ctx: &mut CliContext, args: &ArgMatches) -> anyhow::Result<()> {
    let fleet_name = args.get_one::<String>("FLEET").unwrap();

    info!(fleet = fleet_name, "summoning server from fleet");
    let response = ctx
        .sdk_client
        .allocate_from_fleet(tonic::Request::new(FleetAllocationRequest {
            namespace: ctx.kube_namespace.clone(),
            name: fleet_name.clone(),
            summon_if_needed: true,
            custom_annotations: HashMap::new(),
        }))
        .await?
        .into_inner();

    info!(
        game_server_id = response.game_server_id,
        "successfully created game server"
    );
    Ok(())
}

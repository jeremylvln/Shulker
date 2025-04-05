use anyhow::anyhow;
use backon::ExponentialBuilder;
use backon::Retryable;
use google_agones_crds::v1::{fleet::Fleet, game_server::GameServer};
use kube::api::ListParams;
use kube::{api::PostParams, core::ObjectMeta, Api, Client, Error, Resource, ResourceExt};
use rand::distr::{Alphanumeric, SampleString};
use shulker_crds::v1alpha1::minecraft_server_fleet::MinecraftServerFleet;
use std::collections::HashMap;
use tracing::warn;

const ORDER_TICKET_ANNOTATION: &str = "shulker.io/order-ticket";

async fn wait_for_game_server(
    client: Client,
    ns: &str,
    order_ticket: &str,
) -> Result<GameServer, anyhow::Error> {
    let get_game_server = async || {
        let game_servers = Api::<GameServer>::namespaced(client.clone(), ns)
            .list(
                &ListParams::default().labels(&format!("{ORDER_TICKET_ANNOTATION}={order_ticket}")),
            )
            .await?;

        let game_server = game_servers.items.first();

        game_server
            .map(|gs| gs.to_owned())
            .ok_or_else(|| anyhow!("no game server found with ticket {}", order_ticket))
    };

    get_game_server
        .retry(ExponentialBuilder::default())
        .notify(|_err, _dur| {
            warn!(
                order_ticket = order_ticket,
                "summoned game server is not created yet",
            )
        })
        .await
}

async fn summon_from_fleet(
    client: Client,
    fleet: &Fleet,
    custom_annotations: Option<HashMap<String, String>>,
) -> Result<GameServer, anyhow::Error> {
    let order_ticket = Alphanumeric.sample_string(&mut rand::rng(), 16);

    let mut labels = fleet
        .spec
        .template
        .metadata
        .as_ref()
        .map(|metadata| metadata.labels.clone())
        .unwrap_or_default()
        .unwrap_or_default();
    labels.insert("agones.dev/fleet".to_string(), fleet.name_any());
    labels.insert(ORDER_TICKET_ANNOTATION.to_string(), order_ticket.clone());

    let mut annotations = fleet
        .spec
        .template
        .metadata
        .as_ref()
        .map(|metadata| metadata.annotations.clone())
        .unwrap_or_default()
        .unwrap_or_default();
    if let Some(custom_annotations) = custom_annotations {
        annotations.extend(custom_annotations);
    }

    let mut game_server = GameServer {
        metadata: ObjectMeta {
            generate_name: Some(format!("{}-manual-", fleet.name_any())),
            name: None,
            namespace: fleet.namespace(),
            labels: Some(labels),
            annotations: Some(annotations),
            ..ObjectMeta::default()
        },
        spec: fleet.spec.template.spec.clone(),
        status: None,
    };

    game_server
        .owner_references_mut()
        .push(fleet.controller_owner_ref(&()).unwrap());

    let created_game_server = Api::namespaced(client.clone(), fleet.namespace().as_ref().unwrap())
        .create(
            &PostParams {
                dry_run: false,
                field_manager: Some("shulker-operator".to_string()),
            },
            &game_server,
        )
        .await;

    match created_game_server {
        Ok(server) => Ok(server),
        Err(Error::SerdeError(_)) => {
            wait_for_game_server(client, &fleet.namespace().unwrap(), &order_ticket).await
        }
        Err(err) => Err(err)?,
    }
}

pub async fn summon_from_minecraft_server_fleet(
    client: Client,
    minecraft_server_fleet: &MinecraftServerFleet,
    custom_annotations: Option<HashMap<String, String>>,
) -> Result<GameServer, anyhow::Error> {
    let fleet_api =
        Api::<Fleet>::namespaced(client.clone(), &minecraft_server_fleet.namespace().unwrap());
    let fleet = fleet_api.get(&minecraft_server_fleet.name_any()).await?;

    summon_from_fleet(client, &fleet, custom_annotations).await
}

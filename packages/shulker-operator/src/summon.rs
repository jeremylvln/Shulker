use std::collections::HashMap;

use google_agones_crds::v1::{fleet::Fleet, game_server::GameServer};
use kube::{api::PostParams, core::ObjectMeta, Api, Client, Resource, ResourceExt};
use shulker_crds::v1alpha1::minecraft_server_fleet::MinecraftServerFleet;

async fn summon_from_fleet(
    client: Client,
    fleet: &Fleet,
    custom_annotations: Option<HashMap<String, String>>,
) -> Result<GameServer, anyhow::Error> {
    let mut labels = fleet
        .spec
        .template
        .metadata
        .as_ref()
        .map(|metadata| metadata.labels.clone())
        .unwrap_or_default()
        .unwrap_or_default();
    labels.insert("agones.dev/fleet".to_string(), fleet.name_any());

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
            generate_name: Some(format!("{}-", fleet.name_any())),
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

    let created_game_server = Api::namespaced(client, fleet.namespace().as_ref().unwrap())
        .create(
            &PostParams {
                dry_run: false,
                field_manager: Some("shulker-operator".to_string()),
            },
            &game_server,
        )
        .await?;

    Ok(created_game_server)
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

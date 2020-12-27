use k8s_openapi::api::core::v1::Pod;
use kube::{
    api::{Api, DeleteParams, Meta, PostParams},
    client::Client,
};
use snafu::{ensure, ResultExt, Snafu};
use std::collections::hash_map::DefaultHasher;
use std::hash::{Hash, Hasher};
use tracing::{debug, info};

use shulker_crds::minecraft_server_template::{
    MinecraftServerTemplate, MinecraftServerTemplateSpec,
};

#[derive(Debug, Snafu)]
pub enum Error {
    #[snafu(display("Invalid template {} for MinecraftServer pod: {}", template, reason))]
    InvalidTemplate {
        template: String,
        reason: String,
    },
    #[snafu(display("Failed to create a MinecraftServer pod: {}", source))]
    PodCreationFailed {
        source: kube::Error,
    },
    #[snafu(display("Failed to delete a MinecraftServer pod: {}", source))]
    PodDeletionFailed {
        source: kube::Error,
    },
    #[snafu(display("Failed to find a MinecraftServer pod: {}", source))]
    PodLookupFailed {
        source: kube::Error,
    },
    SerializationFailed {
        source: serde_json::Error,
    },
}

fn is_sync_needed(pod: &Pod, config_hash: &str) -> bool {
    if let None = pod.metadata.labels {
        return true;
    }

    let labels = pod.metadata.labels.as_ref().unwrap();
    let sync_needed = match labels.get("shulker.io/config-hash") {
        Some(existing_hash) => existing_hash != config_hash,
        None => true,
    };

    if sync_needed {
        info!("synchronization needed for pod {}", &Meta::name(pod));
    }
    sync_needed
}

fn validate_template(name: &str, spec: &MinecraftServerTemplateSpec) -> Result<(), Error> {
    ensure!(
        spec.version.is_some(),
        InvalidTemplate {
            template: name.to_owned(),
            reason: "No version provided".to_owned()
        }
    );

    Ok(())
}

fn create_pod_json(
    server_name: &str,
    template: &MinecraftServerTemplate,
) -> Result<(Pod, String), Error> {
    let composed_spec: MinecraftServerTemplateSpec =
        serde_json::from_str(&template.status.as_ref().unwrap().compose_result)
            .context(SerializationFailed)?;
    validate_template(&Meta::name(template), &composed_spec)?;

    let spec = serde_json::json!({
        "containers": [{
            "name": "minecraftserver",
            "image": "ghcr.io/iamblueslime/itzg-minecraft-server-mirror:latest",
            "env": [
                { "name": "CONSOLE", "value": "false" },
                { "name": "GUI", "value": "false" },
                { "name": "OVERRIDE_SERVER_PROPERTIES", "value": "true" },
                { "name": "SERVER_NAME", "value": server_name },
                { "name": "SERVER_PORT", "value": "25565" },
                { "name": "ENABLE_RCON", "value": "true" },
                { "name": "RCON_PORT", "value": "25575" },
                { "name": "RCON_PASSWORD", "value": "shulkerrcon" },
                { "name": "EULA", "value": "true" },
                { "name": "TYPE", "value": composed_spec.version.as_ref().unwrap().channel.to_uppercase() },
                { "name": "VERSION", "value": composed_spec.version.as_ref().unwrap().name.to_uppercase() },
            ],
            "ports": [
                {
                    "name": "players",
                    "containerPort": 25565,
                    "protocol": "TCP"
                }, {
                    "name": "rcon",
                    "containerPort": 25575,
                    "protocol": "TCP"
                }
            ],
        }],
        "restartPolicy": "Never"
    });

    let mut spec_hasher = DefaultHasher::new();
    serde_json::to_string(&spec)
        .context(SerializationFailed)?
        .hash(&mut spec_hasher);
    let spec_hash = format!("{:x}", spec_hasher.finish());

    let pod = serde_json::from_value(serde_json::json!({
        "apiVersion": "v1",
        "kind": "Pod",
        "metadata": {
            "name": server_name,
            "labels": {
                "shulker.io/template-name": &Meta::name(template),
                "shulker.io/config-hash": spec_hash,
            }
        },
        "spec": spec
    }))
    .context(SerializationFailed)?;

    Ok((pod, spec_hash))
}

pub async fn spawn_instance(
    client: Client,
    server_name: &str,
    ns: &str,
    template: &MinecraftServerTemplate,
) -> Result<Pod, Error> {
    let pods: Api<Pod> = Api::namespaced(client.clone(), ns);
    let pod_ressource = create_pod_json(&server_name, template)?;

    match pods.get(server_name).await {
        Ok(existing_pod) => {
            debug!("found existing pod {}", server_name);
            if !is_sync_needed(&existing_pod, &pod_ressource.1) {
                return Ok(existing_pod);
            }

            debug!("deleting existing pod {}", server_name);
            pods.delete(server_name, &DeleteParams::default())
                .await
                .context(PodDeletionFailed)?;
        }
        Err(e) => {
            match e {
                kube::Error::Api(kube::error::ErrorResponse { code: 404, .. }) => {}
                _ => return Err(Error::PodDeletionFailed {
                    source: e
                })
            }
        }
    }

    debug!("creating new pod {}", server_name);
    pods.create(&PostParams::default(), &pod_ressource.0)
        .await
        .context(PodCreationFailed)
}

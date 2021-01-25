use k8s_openapi::api::apps::v1::Deployment;
use kube::{
    api::{Api, Meta, PatchParams, PostParams},
    client::Client,
};
use snafu::{ensure, ResultExt, Snafu};
use std::collections::hash_map::DefaultHasher;
use std::hash::{Hash, Hasher};
use tracing::debug;

use shulker_crds::minecraft_server_template::{
    MinecraftServerTemplate, MinecraftServerTemplateSpec,
};

#[derive(Debug, Snafu)]
pub enum Error {
    #[snafu(display(
        "Invalid template {} for MinecraftServer deployment: {}",
        template,
        reason
    ))]
    TemplateInvalid {
        template: String,
        reason: String,
    },
    #[snafu(display("Failed to create a MinecraftServer deployment: {}", source))]
    DeploymentCreationFailed {
        source: kube::Error,
    },
    #[snafu(display("Failed to delete a MinecraftServer deployment: {}", source))]
    DeploymentPatchFailed {
        source: kube::Error,
    },
    SerializationFailed {
        source: serde_json::Error,
    },
}

fn validate_template(name: &str, spec: &MinecraftServerTemplateSpec) -> Result<(), Error> {
    ensure!(
        spec.version.is_some(),
        TemplateInvalid {
            template: name.to_owned(),
            reason: "No version provided".to_owned()
        }
    );

    ensure!(
        spec.replicas.is_some(),
        TemplateInvalid {
            template: name.to_owned(),
            reason: "No replicas provided".to_owned()
        }
    );

    Ok(())
}

fn create_deployment_json(
    minecraft_server_name: &str,
    deployment_name: &str,
    template: &MinecraftServerTemplate,
) -> Result<(Deployment, serde_json::Value), Error> {
    let composed_spec: MinecraftServerTemplateSpec =
        serde_json::from_str(&template.status.as_ref().unwrap().compose_result)
            .context(SerializationFailed)?;
    validate_template(&Meta::name(template), &composed_spec)?;

    let spec = serde_json::json!({
        "replicas": composed_spec.replicas.as_ref().unwrap().min.unwrap_or(1),
        "selector": {
            "matchLabels": {
                "shulker.io/managed-by": "shulker",
                "shulker.io/template-name": &Meta::name(template),
            }
        },
        "template": {
            "metadata": {
                "labels": {
                    "shulker.io/managed-by": "shulker",
                    "shulker.io/template-name": &Meta::name(template),
                }
            },
            "spec": {
                "containers": [{
                    "name": "minecraftserver",
                    "image": "ghcr.io/iamblueslime/itzg-minecraft-server-mirror:latest",
                    "env": [
                        { "name": "CONSOLE", "value": "false" },
                        { "name": "GUI", "value": "false" },
                        { "name": "OVERRIDE_SERVER_PROPERTIES", "value": "true" },
                        { "name": "SERVER_NAME", "value": deployment_name },
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
                "restartPolicy": "Always"
            }
        }
    });

    let mut spec_hasher = DefaultHasher::new();
    serde_json::to_string(&spec)
        .context(SerializationFailed)?
        .hash(&mut spec_hasher);
    let spec_hash = format!("{:x}", spec_hasher.finish());

    let deployment_json = serde_json::json!({
        "apiVersion": "apps/v1",
        "kind": "Deployment",
        "metadata": {
            "name": deployment_name,
            "labels": {
                "shulker.io/managed-by": "shulker",
                "shulker.io/minecraft-server-name": minecraft_server_name,
                "shulker.io/template-name": &Meta::name(template),
                "shulker.io/config-hash": spec_hash,
            }
        },
        "spec": spec
    });

    let deployment =
        serde_json::from_value(deployment_json.clone()).context(SerializationFailed)?;

    Ok((deployment, deployment_json))
}

pub async fn ensure_deployment(
    client: Client,
    minecraft_server_name: &str,
    deployment_name: &str,
    ns: &str,
    template: &MinecraftServerTemplate,
) -> Result<Deployment, Error> {
    let deployments: Api<Deployment> = Api::namespaced(client.clone(), ns);
    let deployment = create_deployment_json(&minecraft_server_name, &deployment_name, template)?;

    match deployments.get(deployment_name).await {
        Ok(_) => {
            debug!("patching existing deployment {}", deployment_name);
            return deployments
                .patch(
                    deployment_name,
                    &PatchParams::default(),
                    serde_json::to_vec(&deployment.1).context(SerializationFailed)?,
                )
                .await
                .context(DeploymentPatchFailed);
        }
        Err(e) => match e {
            kube::Error::Api(kube::error::ErrorResponse { code: 404, .. }) => {}
            _ => return Err(Error::DeploymentPatchFailed { source: e }),
        },
    }

    debug!("creating new deployment {}", deployment_name);
    deployments
        .create(&PostParams::default(), &deployment.0)
        .await
        .context(DeploymentCreationFailed)
}

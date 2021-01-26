use k8s_openapi::api::apps::v1::Deployment;
use kube::{
    api::{Api, Meta, Patch, PatchParams, PostParams},
    client::Client,
};
use snafu::{ensure, ResultExt, Snafu};
use std::collections::hash_map::DefaultHasher;
use std::hash::{Hash, Hasher};
use tracing::debug;

use shulker_crds::minecraft_server_template::{
    MinecraftServerTemplate, MinecraftServerTemplateSpec,
};

/// Enumeration of possible errors concerning the
/// deployment of a MinecraftServer resource.
#[derive(Debug, Snafu)]
pub enum Error {
    /// The template is invalid, aka. with either
    /// missing or malformed properties.
    #[snafu(display(
        "Invalid template {} for MinecraftServer deployment: {}",
        template,
        reason
    ))]
    TemplateInvalid { template: String, reason: String },
    /// Kubernetes's API rejected the creation of the
    /// Deployment.
    #[snafu(display("Failed to create a MinecraftServer deployment: {}", source))]
    DeploymentCreationFailed { source: kube::Error },
    /// Kubernetes's API rejected the patch of an
    /// existing Deployment.
    #[snafu(display("Failed to patch a MinecraftServer deployment: {}", source))]
    DeploymentPatchFailed { source: kube::Error },
    /// Something went wrong when serializing from or
    /// deserializing to JSON.
    SerializationFailed { source: serde_json::Error },
}

/// Validate a MinecraftServerTemplate spec resource.
///
/// As a template could be partial, we must ensure that
/// the root template linked to the MinecraftServer
/// resource contains all the needed properties.
///
/// # Arguments
///
/// - `name` - Name of the template
/// - `spec` - Template spec
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

/// Create a Deployment spec from a MinecraftServer
/// spec.
///
/// # Arguments
///
/// - `minecraft_server_name` - Name of the MinecraftServer
/// resource
/// - `deployment_name` - Name of the Deployment
/// - `template` - Template to use to compose the Deployment
fn create_deployment(
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

/// Ensure that a Kubernetes Deployment exist and is
/// synced with a MinecraftServer resource.
/// @todo Rely on a MinecraftServer directly
///
/// # Arguments
///
/// - `client` - Kubernetes client
/// - `minecraft_server_name` - Name of the MinecraftServer
/// resource
/// - `deployment_name` - Name of the Deployment
/// - `ns` - Namespace to work on
/// - `template` - Template to use to compose the Deployment
pub async fn ensure_deployment(
    client: Client,
    minecraft_server_name: &str,
    deployment_name: &str,
    ns: &str,
    template: &MinecraftServerTemplate,
) -> Result<Deployment, Error> {
    let deployments: Api<Deployment> = Api::namespaced(client.clone(), ns);
    let deployment = create_deployment(&minecraft_server_name, &deployment_name, template)?;

    match deployments.get(deployment_name).await {
        Ok(_) => {
            debug!("patching existing deployment {}", deployment_name);
            return deployments
                .patch(
                    deployment_name,
                    &PatchParams::default(),
                    &Patch::Merge(&deployment.1),
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

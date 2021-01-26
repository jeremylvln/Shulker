use k8s_openapi::apiextensions_apiserver::pkg::apis::apiextensions::v1beta1::CustomResourceDefinition;
use kube::{api::Api, client::Client, error::Error};

/// Describe the MinecraftServer CRD.
pub mod minecraft_server;
/// Describe the MinecraftServerTemplate CRD.
pub mod minecraft_server_template;
/// Describe a Resource (shared between multiple
/// CRDs).
pub mod resource;
/// Base traits describing a CRD which could be
/// templated (shared between multiple CRDs).
pub mod template;

/// Asserts that the custom CRDs we are using are
/// installed on the Kubernetes cluster.
/// 
/// # Arguments
/// 
/// * `client` - Kubernetes client
pub async fn assert_installed_crds(client: Client) -> Result<(), Error> {
    let crds: Api<CustomResourceDefinition> = Api::all(client.clone());
    crds.get("minecraftservertemplates.shulker.io").await?;
    crds.get("minecraftservers.shulker.io").await?;

    Ok(())
}

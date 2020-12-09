use k8s_openapi::apiextensions_apiserver::pkg::apis::apiextensions::v1beta1::CustomResourceDefinition;
use kube::{api::Api, client::Client, error::Error};

pub mod minecraft_server;
pub mod minecraft_server_template;
pub mod template;

pub async fn assert_installed_crds(client: Client) -> Result<(), Error> {
    let crds: Api<CustomResourceDefinition> = Api::all(client.clone());
    crds.get("minecraftservertemplates.shulker.io").await?;
    crds.get("minecraftservers.shulker.io").await?;

    Ok(())
}

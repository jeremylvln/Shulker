use kube::{api::Api, client::Client, error::Error};

use shulker_crds::minecraft_server_template::MinecraftServerTemplate;

pub mod compose;

pub async fn get_template(
    client: Client,
    name: &str,
    ns: &str,
) -> Result<MinecraftServerTemplate, Error> {
    let templates: Api<MinecraftServerTemplate> = Api::namespaced(client.clone(), ns);
    Ok(templates.get(name).await?)
}

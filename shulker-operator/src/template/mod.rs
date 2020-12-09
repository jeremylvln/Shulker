use kube::{api::Api, client::Client, error::Error};

use shulker_crds::minecraft_server_template::MinecraftServerTemplate;

pub async fn get_template(client: Client, name: &str, ns: &str) -> Result<(), Error> {
    let templates: Api<MinecraftServerTemplate> = Api::namespaced(client.clone(), ns);
    templates.get(name).await?;
    Ok(())
}

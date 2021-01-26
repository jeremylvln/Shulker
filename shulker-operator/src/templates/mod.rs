use kube::{api::Api, client::Client, error::Error};

use shulker_crds::minecraft_server_template::MinecraftServerTemplate;

/// Template composition helpers.
pub mod compose;

/// Get a template from Kubernetes API
/// identified by its namespace-name pair.
///
/// # Arguments
/// - `client` - Kubernertes client
/// - `name` - Name of the template
/// - `ns` - Namespace where the template
/// is stored
pub async fn get_template(
    client: Client,
    name: &str,
    ns: &str,
) -> Result<MinecraftServerTemplate, Error> {
    let templates: Api<MinecraftServerTemplate> = Api::namespaced(client.clone(), ns);
    Ok(templates.get(name).await?)
}

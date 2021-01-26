use futures::{future::BoxFuture, FutureExt};
use kube::{
    api::{Api, Meta},
    client::Client,
    error::Error,
};
use merge::Merge;
use serde::de::DeserializeOwned;

use shulker_crds::template::{Template, TemplateSpec};

/// Compose a template with its parents.
///
/// This helper will retrieve all the parents
/// from the Kubernetes API client and use the
/// `merge` create to fold the properties to the
/// template given as parameter.
///
/// # Arguments
/// - `client` - Kubernetes client
/// - `templates` - Kubernetes API client for the
/// according template resource
/// - `template` - Template to fold the properties in.
fn retreive_inherited_specs<
    'a,
    T: Template<S> + Meta + DeserializeOwned + Clone + Sync + Send,
    S: TemplateSpec + Clone + Send,
>(
    client: Client,
    templates: &'a Api<T>,
    template: &'a T,
) -> BoxFuture<'a, Result<Vec<S>, Error>> {
    async move {
        let parent_names: &Option<Vec<String>> = template.spec().inherit_from();
        if None == parent_names.as_deref() {
            return Ok(vec![]);
        }

        let iter = parent_names.as_ref().unwrap().iter();
        let mut inherited_specs: Vec<S> = vec![];
        let mut visited: Vec<String> = vec![Meta::name(template)];

        for parent_name in iter {
            if !visited.contains(&parent_name) {
                match templates.get(&parent_name).await {
                    Ok(found_parent) => {
                        match retreive_inherited_specs(client.clone(), &templates, &found_parent)
                            .await
                        {
                            Ok(mut found_parents) => {
                                inherited_specs.append(&mut found_parents);
                                inherited_specs.push(found_parent.spec().clone());
                                visited.push(parent_name.clone())
                            }
                            Err(err) => return Err(err),
                        }
                    }
                    Err(err) => return Err(err),
                }
            }
        }

        Ok(inherited_specs)
    }
    .boxed()
}

/// Fold the template given as parameter with
/// the properties of its parents which the
/// template could inherit properties from.
///
/// # Arguments
/// - `client` - Kubernetes client
/// - `template` - Template to fold the properties in.
pub async fn fold_template_spec<
    T: Template<S> + Meta + DeserializeOwned + Clone + Sync + Send,
    S: TemplateSpec + Merge + Clone + Send,
>(
    client: Client,
    template: &T,
) -> Result<S, Error> {
    let templates: Api<T> = Api::namespaced(client.clone(), &Meta::namespace(template).unwrap());

    match retreive_inherited_specs(client.clone(), &templates, template).await {
        Ok(parents) => {
            let default_spec = template.spec().clone();

            Ok(parents.iter().fold(default_spec, |mut spec, parent| {
                spec.merge(parent.clone());
                spec
            }))
        }
        Err(err) => Err(err),
    }
}

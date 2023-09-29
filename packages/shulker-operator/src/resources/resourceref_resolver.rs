use kube::Client;
use url::Url;

use shulker_crds::resourceref::ResourceRefSpec;

use super::{
    http_credentials::HttpCredentials, resourceref::ResourceRef, ResourceRefError, Result,
};

pub struct ResourceRefResolver {
    client: Client,
}

impl ResourceRefResolver {
    pub fn new(client: Client) -> Self {
        ResourceRefResolver { client }
    }

    pub async fn resolve_all(&self, refs: &[ResourceRefSpec]) -> Result<Vec<Url>> {
        let futs = refs.iter().map(|resourceref| self.resolve(resourceref));

        futures::future::join_all(futs).await.into_iter().try_fold(
            Vec::new(),
            |mut acc, resourceref| {
                if resourceref.is_err() {
                    return Err(resourceref.err().unwrap());
                }

                let url = resourceref.unwrap().as_url()?;
                acc.push(url);
                Ok::<Vec<Url>, ResourceRefError>(acc)
            },
        )
    }

    pub async fn resolve(&self, spec: &ResourceRefSpec) -> Result<ResourceRef> {
        if let Some(url) = &spec.url {
            return Ok(ResourceRef::Url(url.clone()));
        } else if let Some(url_from) = &spec.url_from {
            if let Some(maven_ref) = &url_from.maven_ref {
                let credentials = match &maven_ref.credentials_secret_name {
                    None => None,
                    Some(secret_name) => {
                        let secrets: kube::Api<k8s_openapi::api::core::v1::Secret> =
                            kube::Api::default_namespaced(self.client.clone());

                        let secret = secrets
                            .get(secret_name)
                            .await
                            .map_err(ResourceRefError::FailedToResolveSecret)?;

                        Some(HttpCredentials::from_kubernetes_secret(&secret)?)
                    }
                };

                return Ok(ResourceRef::MavenArtifact {
                    repository_url: maven_ref.repository_url.clone(),
                    group_id: maven_ref.group_id.clone(),
                    artifact_id: maven_ref.artifact_id.clone(),
                    version: maven_ref.version.clone(),
                    classifier: maven_ref.classifier.clone(),
                    credentials,
                });
            }
        }

        Err(ResourceRefError::InvalidSpec)
    }
}

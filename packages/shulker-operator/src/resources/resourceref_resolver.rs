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

#[cfg(test)]
mod tests {
    use http::{Request, Response};
    use hyper::Body;
    use k8s_openapi::{api::core::v1::Secret, ByteString};
    use kube::{core::ObjectMeta, Client, ResourceExt};
    use shulker_crds::resourceref::{ResourceRefFromSpec, ResourceRefSpec};
    use url::Url;

    use crate::resources::resourceref::ResourceRef;

    type ApiServerHandle = tower_test::mock::Handle<Request<Body>, Response<Body>>;
    struct ApiServerVerifier(ApiServerHandle);

    enum Scenario {
        SecretFound(Secret),
        SecretNotFound,
    }

    impl ApiServerVerifier {
        pub fn run(self, scenario: Scenario) -> tokio::task::JoinHandle<()> {
            tokio::spawn(async move {
                // moving self => one scenario per test
                match scenario {
                    Scenario::SecretFound(secret) => self.handle_secret_found(secret).await,
                    Scenario::SecretNotFound => self.handle_secret_not_found().await,
                }
                .expect("scenario completed without errors");
            })
        }

        async fn handle_secret_found(mut self, secret: Secret) -> Result<Self, anyhow::Error> {
            let (request, send) = self.0.next_request().await.expect("service not called");
            assert_eq!(request.method(), http::Method::GET);
            assert_eq!(
                request.uri().to_string(),
                format!("/api/v1/namespaces/default/secrets/{}", secret.name_any())
            );

            let response = serde_json::to_vec(&secret).unwrap();
            send.send_response(Response::builder().body(Body::from(response)).unwrap());
            Ok(self)
        }

        async fn handle_secret_not_found(mut self) -> Result<Self, anyhow::Error> {
            let (request, send) = self.0.next_request().await.expect("service not called");
            assert_eq!(request.method(), http::Method::GET);
            assert_eq!(
                request.uri().to_string(),
                "/api/v1/namespaces/default/secrets/not_existing_secret".to_string()
            );

            send.send_response(Response::builder().status(404).body(Body::empty()).unwrap());
            Ok(self)
        }
    }

    fn create_client_mock() -> (Client, ApiServerVerifier) {
        let (mock_service, handle) = tower_test::mock::pair::<Request<Body>, Response<Body>>();
        let mock_client = Client::new(mock_service, "default");
        (mock_client, ApiServerVerifier(handle))
    }

    async fn timeout_after_1s(handle: tokio::task::JoinHandle<()>) {
        tokio::time::timeout(std::time::Duration::from_secs(1), handle)
            .await
            .expect("timeout on mock apiserver")
            .expect("scenario succeeded")
    }

    #[tokio::test]
    async fn resolve_all() {
        // G
        let (client, _) = create_client_mock();
        let resourceref_resolver = super::ResourceRefResolver::new(client);
        let url = "https://example.com/file.tar.gz".to_string();
        let resourceref_spec = ResourceRefSpec {
            url: Some(url.clone()),
            ..ResourceRefSpec::default()
        };

        // W
        let resourceref = resourceref_resolver
            .resolve_all(&[resourceref_spec])
            .await
            .unwrap();

        // T
        assert_eq!(resourceref, vec![Url::parse(&url).unwrap()])
    }

    #[tokio::test]
    async fn resolve_url() {
        // G
        let (client, _) = create_client_mock();
        let resourceref_resolver = super::ResourceRefResolver::new(client);
        let url = "https://example.com/file.tar.gz".to_string();
        let resourceref_spec = ResourceRefSpec {
            url: Some(url.clone()),
            ..ResourceRefSpec::default()
        };

        // W
        let resourceref = resourceref_resolver
            .resolve(&resourceref_spec)
            .await
            .unwrap();

        // T
        assert_eq!(resourceref, ResourceRef::Url(url))
    }

    #[tokio::test]
    async fn resolve_url_from_maven() {
        // G
        let (client, _) = create_client_mock();
        let resourceref_resolver = super::ResourceRefResolver::new(client);
        let (repository_url, group_id, artifact_id, version, classifier) = (
            "https://example.com/maven".to_string(),
            "com.example".to_string(),
            "myartifact".to_string(),
            "1.0.0".to_string(),
            "api".to_string(),
        );
        let resourceref_spec = ResourceRefSpec {
            url_from: Some(ResourceRefFromSpec {
                maven_ref: Some(shulker_crds::resourceref::ResourceRefFromMavenSpec {
                    repository_url: repository_url.clone(),
                    group_id: group_id.clone(),
                    artifact_id: artifact_id.clone(),
                    version: version.clone(),
                    classifier: Some(classifier.clone()),
                    credentials_secret_name: None,
                }),
            }),
            ..ResourceRefSpec::default()
        };

        // W
        let resourceref = resourceref_resolver
            .resolve(&resourceref_spec)
            .await
            .unwrap();

        // T
        assert_eq!(
            resourceref,
            ResourceRef::MavenArtifact {
                repository_url,
                group_id,
                artifact_id,
                version,
                classifier: Some(classifier),
                credentials: None,
            }
        )
    }

    #[tokio::test]
    async fn resolve_url_from_maven_with_credentials() {
        // G
        let (client, server) = create_client_mock();
        let resourceref_resolver = super::ResourceRefResolver::new(client);
        let credentials_secret = Secret {
            metadata: ObjectMeta {
                name: Some("my_super_secret".to_string()),
                ..ObjectMeta::default()
            },
            type_: Some("Opaque".to_string()),
            data: Some(std::collections::BTreeMap::from([
                (
                    "username".to_string(),
                    ByteString("user".as_bytes().to_vec()),
                ),
                (
                    "password".to_string(),
                    ByteString("pass".as_bytes().to_vec()),
                ),
            ])),
            ..Secret::default()
        };
        let (repository_url, group_id, artifact_id, version, classifier) = (
            "https://example.com/maven".to_string(),
            "com.example".to_string(),
            "myartifact".to_string(),
            "1.0.0".to_string(),
            "api".to_string(),
        );
        let resourceref_spec = ResourceRefSpec {
            url_from: Some(ResourceRefFromSpec {
                maven_ref: Some(shulker_crds::resourceref::ResourceRefFromMavenSpec {
                    repository_url: repository_url.clone(),
                    group_id: group_id.clone(),
                    artifact_id: artifact_id.clone(),
                    version: version.clone(),
                    classifier: Some(classifier.clone()),
                    credentials_secret_name: Some(credentials_secret.name_any()),
                }),
            }),
            ..ResourceRefSpec::default()
        };
        let run_server = server.run(Scenario::SecretFound(credentials_secret));

        // W
        let resourceref = resourceref_resolver
            .resolve(&resourceref_spec)
            .await
            .unwrap();

        // T
        timeout_after_1s(run_server).await;
        assert_eq!(
            resourceref,
            ResourceRef::MavenArtifact {
                repository_url,
                group_id,
                artifact_id,
                version,
                classifier: Some(classifier),
                credentials: Some(super::HttpCredentials {
                    username: "user".to_string(),
                    password: "pass".to_string(),
                })
            }
        )
    }

    #[tokio::test]
    #[should_panic(expected = "FailedToResolveSecret")]
    async fn resolve_url_from_maven_with_credentials_not_found() {
        // G
        let (client, server) = create_client_mock();
        let resourceref_resolver = super::ResourceRefResolver::new(client);
        let (repository_url, group_id, artifact_id, version, classifier) = (
            "https://example.com/maven".to_string(),
            "com.example".to_string(),
            "myartifact".to_string(),
            "1.0.0".to_string(),
            "api".to_string(),
        );
        let resourceref_spec = ResourceRefSpec {
            url_from: Some(ResourceRefFromSpec {
                maven_ref: Some(shulker_crds::resourceref::ResourceRefFromMavenSpec {
                    repository_url: repository_url.clone(),
                    group_id: group_id.clone(),
                    artifact_id: artifact_id.clone(),
                    version: version.clone(),
                    classifier: Some(classifier.clone()),
                    credentials_secret_name: Some("not_existing_secret".to_string()),
                }),
            }),
            ..ResourceRefSpec::default()
        };
        server.run(Scenario::SecretNotFound);

        // W
        resourceref_resolver
            .resolve(&resourceref_spec)
            .await
            .unwrap();
    }
}

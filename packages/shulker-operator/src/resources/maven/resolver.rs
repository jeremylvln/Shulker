use http::StatusCode;
use reqwest::Client;
use thiserror::Error;
use tracing::*;

use crate::resources::http_credentials::HttpCredentials;

use super::MavenMetadata;

#[derive(Error, Debug)]
pub enum ResolverError {
    #[error("failed to perform request: {0}")]
    FailedToRequestUnderlying(#[source] reqwest::Error),

    #[error("failed to request, got status {0}: {1}")]
    FailedToRequest(StatusCode, String),

    #[error("failed to parse Maven metadata: {0}")]
    FailedToParseMetadata(#[source] serde_xml_rs::Error),
}

pub type Result<T, E = ResolverError> = std::result::Result<T, E>;

pub struct MavenResolver {
    repository_url: String,
    credentials: Option<HttpCredentials>,
    http_client: Client,
}

impl MavenResolver {
    pub fn new(repository_url: String, credentials: Option<HttpCredentials>) -> Self {
        MavenResolver {
            repository_url,
            credentials,
            http_client: Client::new(),
        }
    }

    pub async fn try_resolve_artifact_metadata(
        &self,
        group_id: &str,
        artifact_id: &str,
    ) -> Result<Option<MavenMetadata>> {
        let url = format!(
            "{}/{}/{}/maven-metadata.xml",
            self.repository_url,
            group_id.replace('.', "/"),
            artifact_id
        );

        self.try_resolve_metadata(url).await
    }

    pub async fn try_resolve_version_metadata(
        &self,
        group_id: &str,
        artifact_id: &str,
        version: &str,
    ) -> Result<Option<MavenMetadata>> {
        let url = format!(
            "{}/{}/{}/{}/maven-metadata.xml",
            self.repository_url,
            group_id.replace('.', "/"),
            artifact_id,
            version
        );

        self.try_resolve_metadata(url).await
    }

    async fn try_resolve_metadata(&self, url: String) -> Result<Option<MavenMetadata>> {
        debug!(url = url, "trying to resolve Maven metadata");
        let mut request_builder = self.http_client.get(url);
        if let Some(credentials) = &self.credentials {
            request_builder = request_builder.basic_auth(
                credentials.username.clone(),
                Some(credentials.password.clone()),
            );
        }

        let response = request_builder
            .send()
            .await
            .map_err(ResolverError::FailedToRequestUnderlying)?;

        if !response.status().is_success() {
            if response.status().as_u16() == 404 {
                return Ok(None);
            }

            return Err(ResolverError::FailedToRequest(
                response.status(),
                response
                    .text()
                    .await
                    .map_err(ResolverError::FailedToRequestUnderlying)?,
            ));
        }

        let response_str = response
            .text()
            .await
            .map_err(ResolverError::FailedToRequestUnderlying)?;

        serde_xml_rs::from_str(&response_str).map_err(ResolverError::FailedToParseMetadata)
    }
}

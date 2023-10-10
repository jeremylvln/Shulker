use super::{http_credentials::HttpCredentials, ResourceRefError, Result};

pub enum ResourceRef {
    Url(String),
    MavenArtifact {
        repository_url: String,
        group_id: String,
        artifact_id: String,
        version: String,
        classifier: Option<String>,
        credentials: Option<HttpCredentials>,
    },
}

impl ResourceRef {
    pub fn as_url(&self) -> Result<url::Url> {
        match self {
            ResourceRef::Url(url) => {
                Ok(url::Url::parse(url).map_err(ResourceRefError::InvalidUrlSpec)?)
            }
            ResourceRef::MavenArtifact {
                repository_url,
                group_id,
                artifact_id,
                version,
                classifier,
                credentials,
            } => {
                let group_path = group_id.replace('.', "/");
                let file_name = match classifier {
                    None => format!("{}-{}.jar", artifact_id, version),
                    Some(classifier) => format!("{}-{}-{}.jar", artifact_id, version, classifier),
                };

                let mut url = url::Url::parse(&format!(
                    "{}/{}/{}/{}/{}",
                    repository_url, group_path, artifact_id, version, file_name
                ))
                .map_err(ResourceRefError::InvalidUrlSpec)?;

                if let Some(credentials) = &credentials {
                    url.set_username(&credentials.username).unwrap();
                    url.set_password(Some(&credentials.password)).unwrap();
                }

                Ok(url)
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use crate::resources::http_credentials::HttpCredentials;

    #[test]
    fn serialize_url() {
        // G
        let resourceref = super::ResourceRef::Url("https://example.com/myfile.tar.gz".to_string());

        // W
        let url = resourceref.as_url().unwrap();

        // T
        assert_eq!(url.to_string(), "https://example.com/myfile.tar.gz");
    }

    #[test]
    fn serialize_mavenartifact_without_classifier_credentials() {
        // G
        let resourceref = super::ResourceRef::MavenArtifact {
            repository_url: "https://example.com".to_string(),
            group_id: "io.shulkermc".to_string(),
            artifact_id: "test".to_string(),
            version: "1.0.0".to_string(),
            classifier: None,
            credentials: None,
        };

        // W
        let url = resourceref.as_url().unwrap();

        // T
        assert_eq!(
            url.to_string(),
            "https://example.com/io/shulkermc/test/1.0.0/test-1.0.0.jar"
        );
    }

    #[test]
    fn serialize_mavenartifact_with_classifier_without_credentials() {
        // G
        let resourceref = super::ResourceRef::MavenArtifact {
            repository_url: "https://example.com".to_string(),
            group_id: "io.shulkermc".to_string(),
            artifact_id: "test".to_string(),
            version: "1.0.0".to_string(),
            classifier: Some("api".to_string()),
            credentials: None,
        };

        // W
        let url = resourceref.as_url().unwrap();

        // T
        assert_eq!(
            url.to_string(),
            "https://example.com/io/shulkermc/test/1.0.0/test-1.0.0-api.jar"
        );
    }

    #[test]
    fn serialize_mavenartifact_with_classifier_credentials() {
        // G
        let resourceref = super::ResourceRef::MavenArtifact {
            repository_url: "https://example.com".to_string(),
            group_id: "io.shulkermc".to_string(),
            artifact_id: "test".to_string(),
            version: "1.0.0".to_string(),
            classifier: Some("api".to_string()),
            credentials: Some(HttpCredentials {
                username: "user".to_string(),
                password: "pass".to_string(),
            }),
        };

        // W
        let url = resourceref.as_url().unwrap();

        // T
        assert_eq!(
            url.to_string(),
            "https://user:pass@example.com/io/shulkermc/test/1.0.0/test-1.0.0-api.jar"
        );
    }
}

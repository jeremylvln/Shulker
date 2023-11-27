use thiserror::Error;

pub mod http_credentials;
pub mod maven;
pub mod resourceref;
pub mod resourceref_resolver;

#[derive(Error, Debug)]
pub enum ResourceRefError {
    #[error("failed to resolve Kubernetes Secret: {0}")]
    FailedToResolveSecret(#[source] kube::Error),

    #[error("invalid Kubernetes Secret content: {0}")]
    InvalidSecretSpec(String),

    #[error("invalid generated URL for a resource: {0}")]
    InvalidUrlSpec(#[source] url::ParseError),

    #[error("failed to resolve Maven metadata: {0}")]
    FailedToResolveMavenMetadata(#[source] maven::resolver::ResolverError),

    #[error("invalid resource ref")]
    InvalidSpec,
}

pub type Result<T, E = ResourceRefError> = std::result::Result<T, E>;

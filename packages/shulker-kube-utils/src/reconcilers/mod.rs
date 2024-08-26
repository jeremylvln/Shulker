use thiserror::Error;

pub mod builder;
pub mod status;

#[derive(Error, Debug)]
pub enum BuilderReconcilerError {
    #[error("builder {0} rejected validation of spec: {1}")]
    ValidationError(&'static str, String),

    #[error("builder {0} failed to build resource: {1}")]
    BuilderError(&'static str, #[source] anyhow::Error),

    #[error("failed to update resource status: {0}")]
    FailedToUpdateStatus(#[source] kube::Error),
}

pub type Result<T, E = BuilderReconcilerError> = std::result::Result<T, E>;

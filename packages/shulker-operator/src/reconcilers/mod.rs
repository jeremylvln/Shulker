use thiserror::Error;

pub mod builder;
mod cluster_ref;
pub mod minecraft_cluster;
pub mod minecraft_server;
pub mod minecraft_server_fleet;
pub mod proxy_fleet;
pub mod status;

#[derive(Error, Debug)]
pub enum ReconcilerError {
    #[error("failed to reconcile resource: {0}")]
    FinalizerError(#[source] Box<kube::runtime::finalizer::Error<ReconcilerError>>),

    #[error("failed to update resource status: {0}")]
    FailedToUpdateStatus(#[source] kube::Error),

    #[error("failed to delete stale resource: {0}")]
    FailedToDeleteStale(#[source] kube::Error),

    #[error("failed to resolve cluster ref: {1}")]
    InvalidClusterRef(String, #[source] kube::Error),

    #[error("builder {0} failed to build resource: {1}")]
    BuilderError(&'static str, #[source] anyhow::Error),
}

pub type Result<T, E = ReconcilerError> = std::result::Result<T, E>;

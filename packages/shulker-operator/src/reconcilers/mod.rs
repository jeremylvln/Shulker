use thiserror::Error;

mod cluster_ref;
pub mod minecraft_cluster;
pub mod minecraft_server;
pub mod minecraft_server_fleet;
pub mod proxy_fleet;
mod redis_ref;

#[derive(Error, Debug)]
pub enum ReconcilerError {
    #[error("failed to reconcile resource: {0}")]
    FinalizerError(#[source] Box<kube::runtime::finalizer::Error<ReconcilerError>>),

    #[error("failed to resolve cluster ref: {1}")]
    InvalidClusterRef(String, #[source] kube::Error),

    #[error("failed to build resource: {0}")]
    BuilderError(#[source] shulker_kube_utils::reconcilers::BuilderReconcilerError),

    #[error("failed to delete stale resource: {0}")]
    FailedToDeleteStale(#[source] kube::Error),
}

pub type Result<T, E = ReconcilerError> = std::result::Result<T, E>;

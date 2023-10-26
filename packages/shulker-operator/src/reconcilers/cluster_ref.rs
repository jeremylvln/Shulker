use kube::{Api, Client};
use shulker_crds::v1alpha1::minecraft_cluster::{MinecraftCluster, MinecraftClusterRef};

use super::{ReconcilerError, Result};

pub async fn resolve_cluster_ref(
    client: &Client,
    namespace: &str,
    cluster_ref: &MinecraftClusterRef,
) -> Result<MinecraftCluster> {
    let clusters_api: Api<MinecraftCluster> = Api::namespaced(client.clone(), namespace);
    let cluster = clusters_api
        .get(&cluster_ref.name)
        .await
        .map_err(|e| ReconcilerError::InvalidClusterRef(cluster_ref.name.clone(), e))?;

    Ok(cluster)
}

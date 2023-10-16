use http::{Request, Response};
use hyper::Body;
use kube::{core::ObjectMeta, Client};
use shulker_crds::v1alpha1::minecraft_cluster::{MinecraftCluster, MinecraftClusterSpec};

lazy_static! {
    pub static ref TEST_CLUSTER: MinecraftCluster = MinecraftCluster {
        metadata: ObjectMeta {
            namespace: Some("default".to_string()),
            name: Some("my-cluster".to_string()),
            ..ObjectMeta::default()
        },
        spec: MinecraftClusterSpec {},
        status: None,
    };
}

pub fn create_client_mock() -> Client {
    let (mock_service, _) = tower_test::mock::pair::<Request<Body>, Response<Body>>();
    Client::new(mock_service, "default")
}

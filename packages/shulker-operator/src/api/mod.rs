use std::pin::Pin;

use futures::Future;
use shulker_sdk::minecraft_server_fleet_service_server::MinecraftServerFleetServiceServer;

mod minecraft_server_fleet_grpc;

pub fn create_grpc_server(
    addr: String,
    client: kube::Client,
) -> Pin<Box<dyn Future<Output = Result<(), tonic::transport::Error>>>> {
    Box::pin(
        tonic::transport::Server::builder()
            .add_service(MinecraftServerFleetServiceServer::new(
                minecraft_server_fleet_grpc::MinecraftServerFleetServiceGrpc::new(client.clone()),
            ))
            .serve(addr.parse().unwrap()),
    )
}

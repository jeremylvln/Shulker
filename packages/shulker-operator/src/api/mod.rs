use std::pin::Pin;

use futures::Future;
use google_agones_sdk::allocation::allocation_service_client::AllocationServiceClient;
use shulker_sdk::sdk_service_server::SdkServiceServer;
use tonic::transport::Channel;

mod sdk_grpc;

#[derive(Clone)]
pub struct GrpcServerContext {
    pub client: kube::Client,
    pub agones_allocator_client: AllocationServiceClient<Channel>,
}

pub fn create_grpc_server(
    addr: String,
    context: GrpcServerContext,
) -> Pin<Box<dyn Future<Output = Result<(), tonic::transport::Error>>>> {
    Box::pin(
        tonic::transport::Server::builder()
            .add_service(SdkServiceServer::new(sdk_grpc::SdkServiceGrpc::new(
                context.clone(),
            )))
            .serve(addr.parse().unwrap()),
    )
}

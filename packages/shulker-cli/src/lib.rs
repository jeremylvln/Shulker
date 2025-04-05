use kube::Client;
use shulker_sdk::sdk_service_client::SdkServiceClient;
use tonic::transport::Channel;

pub mod commands;

pub struct CliContext {
    pub kube_client: Client,
    pub kube_namespace: String,
    pub sdk_client: SdkServiceClient<Channel>,
}

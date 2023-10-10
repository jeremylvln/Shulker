use kube::{Api, ResourceExt};
use shulker_crds::v1alpha1::minecraft_server_fleet::MinecraftServerFleet;
use shulker_sdk::{
    minecraft_server_fleet_service_server::MinecraftServerFleetService, SummonFromFleetReply,
    SummonFromFleetRequest,
};
use tonic::{Request, Response, Status};

use crate::summon::summon_from_minecraft_server_fleet;

pub struct MinecraftServerFleetServiceGrpc {
    client: kube::Client,
}

impl MinecraftServerFleetServiceGrpc {
    pub fn new(client: kube::Client) -> Self {
        Self { client }
    }
}

#[tonic::async_trait]
impl MinecraftServerFleetService for MinecraftServerFleetServiceGrpc {
    async fn summon_from_fleet(
        &self,
        request: Request<SummonFromFleetRequest>,
    ) -> Result<Response<SummonFromFleetReply>, Status> {
        let fleet_api = Api::<MinecraftServerFleet>::namespaced(
            self.client.clone(),
            &request.get_ref().namespace,
        );

        let fleet = fleet_api
            .get(&request.get_ref().name)
            .await
            .map_err(|e| match e {
                kube::Error::Api(_) => Status::not_found(format!(
                    "fleet {} in namespace {} not found",
                    &request.get_ref().name,
                    &request.get_ref().namespace,
                )),
                e => Status::internal(format!(
                    "failed to get fleet {} in namespace {}: {}",
                    &request.get_ref().name,
                    &request.get_ref().namespace,
                    e
                )),
            })?;

        let created_game_server = summon_from_minecraft_server_fleet(self.client.clone(), &fleet)
            .await
            .map_err(|e| Status::internal(format!("failed to summon game server: {}", e)))?;

        Ok(Response::new(SummonFromFleetReply {
            game_server_id: created_game_server.name_any(),
        }))
    }
}

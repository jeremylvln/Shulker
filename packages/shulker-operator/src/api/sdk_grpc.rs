use std::collections::HashMap;

use google_agones_sdk::allocation::{AllocationRequest, GameServerSelector, MetaPatch};
use kube::{Api, ResourceExt};
use shulker_crds::v1alpha1::minecraft_server_fleet::MinecraftServerFleet;
use shulker_sdk::{sdk_service_server::SdkService, FleetAllocationReply, FleetAllocationRequest};
use tonic::{Request, Response, Status};

use crate::summon::summon_from_minecraft_server_fleet;

use super::GrpcServerContext;

pub struct SdkServiceGrpc {
    context: GrpcServerContext,
}

impl SdkServiceGrpc {
    pub fn new(context: GrpcServerContext) -> Self {
        Self { context }
    }
}

#[tonic::async_trait]
impl SdkService for SdkServiceGrpc {
    async fn allocate_from_fleet(
        &self,
        request: Request<FleetAllocationRequest>,
    ) -> Result<Response<FleetAllocationReply>, Status> {
        let fleet_api = Api::<MinecraftServerFleet>::namespaced(
            self.context.client.clone(),
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

        let existing_game_server_id = self
            .context
            .agones_allocator_client
            .clone()
            .allocate(AllocationRequest {
                namespace: request.get_ref().namespace.to_string(),
                game_server_selectors: vec![GameServerSelector {
                    match_labels: HashMap::from([(
                        "minecraftserverfleet.shulkermc.io/name".to_string(),
                        request.get_ref().name.clone(),
                    )]),
                    ..GameServerSelector::default()
                }],
                metadata: Some(MetaPatch {
                    annotations: request.get_ref().custom_annotations.clone(),
                    ..MetaPatch::default()
                }),
                ..AllocationRequest::default()
            })
            .await
            .map(|r| r.into_inner().game_server_name);

        let game_server_id = if let Ok(existing_game_server_id) = existing_game_server_id {
            existing_game_server_id
        } else if request.get_ref().summon_if_needed {
            summon_from_minecraft_server_fleet(
                self.context.client.clone(),
                &fleet,
                Some(request.get_ref().custom_annotations.clone()),
            )
            .await
            .map_err(|e| Status::internal(format!("failed to summon game server: {}", e)))?
            .name_any()
        } else {
            return Err(Status::resource_exhausted(format!(
                "no game server available for fleet {} in namespace {}",
                &request.get_ref().name,
                &request.get_ref().namespace,
            )));
        };

        Ok(Response::new(FleetAllocationReply { game_server_id }))
    }
}

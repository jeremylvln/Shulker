use std::pin::Pin;

use actix_web::{middleware, App, HttpServer};
use futures::Future;
use shulker_sdk::minecraft_server_fleet_service_server::MinecraftServerFleetServiceServer;

mod metrics_handlers;
mod minecraft_server_fleet_grpc;

pub fn create_metrics_server(addr: String) -> Result<actix_web::dev::Server, anyhow::Error> {
    Ok(HttpServer::new(move || {
        App::new()
            .wrap(middleware::Logger::default().exclude("/healthz"))
            .service(metrics_handlers::healthz)
            .service(metrics_handlers::metrics)
    })
    .bind(addr)?
    .shutdown_timeout(5)
    .run())
}

pub fn create_grpc_server(
    addr: String,
    client: kube::Client,
) -> Pin<Box<dyn Future<Output = Result<(), tonic::transport::Error>>>> {
    // TODO: dynamic ip & port
    Box::pin(
        tonic::transport::Server::builder()
            .add_service(MinecraftServerFleetServiceServer::new(
                minecraft_server_fleet_grpc::MinecraftServerFleetServiceGrpc::new(client.clone()),
            ))
            .serve(addr.parse().unwrap()),
    )
}

use std::pin::Pin;

use actix_web::{get, middleware, App, HttpRequest, HttpResponse, HttpServer, Responder};
use futures::Future;
use shulker_sdk::minecraft_server_fleet_service_server::MinecraftServerFleetServiceServer;

mod minecraft_server_fleet_grpc;

#[get("/healthz")]
async fn healthz(_: HttpRequest) -> impl Responder {
    HttpResponse::Ok().body("ok")
}

pub fn create_http_server() -> Result<actix_web::dev::Server, anyhow::Error> {
    // TODO: dynamic ip & port
    Ok(HttpServer::new(move || {
        App::new()
            .wrap(middleware::Logger::default().exclude("/healthz"))
            .service(healthz)
    })
    .bind("0.0.0.0:8080")?
    .shutdown_timeout(5)
    .run())
}

pub fn create_grpc_server(
    client: kube::Client,
) -> Pin<Box<dyn Future<Output = Result<(), tonic::transport::Error>>>> {
    // TODO: dynamic ip & port
    Box::pin(
        tonic::transport::Server::builder()
            .add_service(MinecraftServerFleetServiceServer::new(
                minecraft_server_fleet_grpc::MinecraftServerFleetServiceGrpc::new(client.clone()),
            ))
            .serve("0.0.0.0:8081".parse().unwrap()),
    )
}

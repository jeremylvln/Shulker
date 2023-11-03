use actix_web::{get, HttpRequest, HttpResponse, Responder};
use actix_web::{middleware, App, HttpServer};
use tracing::*;

pub fn create_http_server(addr: String) -> Result<actix_web::dev::Server, anyhow::Error> {
    Ok(HttpServer::new(move || {
        App::new()
            .wrap(middleware::Logger::default().exclude("/healthz"))
            .service(healthz)
            .service(metrics)
    })
    .bind(addr)?
    .shutdown_timeout(5)
    .run())
}

#[get("/healthz")]
async fn healthz(_: HttpRequest) -> impl Responder {
    HttpResponse::Ok().body("ok")
}

#[get("/metrics")]
async fn metrics(_: HttpRequest) -> impl Responder {
    let encoder = prometheus::TextEncoder::new();
    let metric_families = prometheus::gather();
    let metric_str = encoder.encode_to_string(&metric_families);

    match metric_str {
        Ok(metric_str) => HttpResponse::Ok()
            .content_type("application/json")
            .body(metric_str),
        Err(e) => {
            error!("failed to encode prometheus metrics: {}", e);
            HttpResponse::InternalServerError().finish()
        }
    }
}

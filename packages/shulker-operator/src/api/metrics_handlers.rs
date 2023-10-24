use actix_web::{get, HttpRequest, HttpResponse, Responder};
use tracing::*;

#[get("/healthz")]
pub async fn healthz(_: HttpRequest) -> impl Responder {
    HttpResponse::Ok().body("ok")
}

#[get("/metrics")]
pub async fn metrics(_: HttpRequest) -> impl Responder {
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

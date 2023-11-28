use axum::body::Body;
use axum::http::StatusCode;
use axum::response::Response;
use axum::{routing::get, Router};
use tracing::*;

pub fn create_http_server(addr: String) -> Result<tokio::task::JoinHandle<()>, anyhow::Error> {
    let task = tokio::spawn(async move {
        let router = Router::new()
            .route("/healthz", get(healthz))
            .route("/metrics", get(metrics));

        let listener = tokio::net::TcpListener::bind(addr).await.unwrap();

        axum::serve(listener, router).await.unwrap()
    });

    Ok(task)
}

async fn healthz() -> (StatusCode, &'static str) {
    (StatusCode::OK, "ok")
}

async fn metrics() -> Response {
    let encoder = prometheus::TextEncoder::new();
    let metric_families = prometheus::gather();
    let metric_str = encoder.encode_to_string(&metric_families);

    match metric_str {
        Ok(metric_str) => Response::builder()
            .status(StatusCode::OK)
            .body(Body::from(metric_str))
            .unwrap(),
        Err(e) => {
            error!("failed to encode prometheus metrics: {}", e);
            Response::builder()
                .status(StatusCode::INTERNAL_SERVER_ERROR)
                .body(Body::from("failed to encode prometheus metrics"))
                .unwrap()
        }
    }
}

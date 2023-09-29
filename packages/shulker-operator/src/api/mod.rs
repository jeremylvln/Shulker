use std::io::Result;

use actix_web::{get, middleware, App, HttpRequest, HttpResponse, HttpServer, Responder};

#[get("/healthz")]
async fn healthz(_: HttpRequest) -> impl Responder {
    HttpResponse::Ok().body("ok")
}

pub fn create_http_server() -> Result<actix_web::dev::Server> {
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

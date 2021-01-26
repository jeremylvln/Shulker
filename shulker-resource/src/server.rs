use serde::Deserialize;

use actix_web::{
    get,
    web::{Data, Path},
    App, Result, HttpServer, Responder,
};
use actix_files::NamedFile;
use std::path::PathBuf;
use std::sync::{Arc, RwLock};

use crate::storage::ResourceStorage;

struct WebServerData {
    dir: PathBuf,
}

#[derive(Deserialize)]
struct GetFromCacheParams {
    provider: String,
    hash: String,
}

#[get("/{provider}/{hash}")]
async fn index(data: Data<WebServerData>, params: Path<GetFromCacheParams>) -> Result<impl Responder> {
    let mut path = data.as_ref().dir.clone();
    path.push(&params.as_ref().provider);
    path.push(&params.as_ref().hash);

    Ok(NamedFile::open(path)?)
}

pub async fn create_http_server(
    port: u16,
    resource_storage: Arc<RwLock<ResourceStorage>>,
) -> Result<(), std::io::Error> {
    HttpServer::new(move || {
        App::new()
            .data(WebServerData {
                dir: resource_storage.read().unwrap().dir.clone(),
            })
            .service(index)
    })
    .bind(format!("0.0.0.0:{}", port))
    .expect("Can not bind web server")
    .shutdown_timeout(0)
    .run()
    .await
}

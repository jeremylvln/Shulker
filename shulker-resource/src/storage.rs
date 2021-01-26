use snafu::{ResultExt, Snafu};
use std::collections::HashMap;
use std::path::PathBuf;
use std::sync::Arc;
use tokio::sync::RwLock;
use tracing::debug;

use crate::provider::{self, ResourceProvider};
use crate::proxy::{self, ResourceProxy};

#[derive(Debug, Snafu)]
pub enum Error {
    ProxyError { source: proxy::Error },
    ProviderError { source: provider::Error },
}

pub struct ResourceStorage {
    pub(crate) dir: PathBuf,
    proxies: HashMap<String, Arc<RwLock<ResourceProxy>>>,
}

impl ResourceStorage {
    pub fn new(dir: &str) -> Self {
        ResourceStorage {
            dir: PathBuf::from(dir),
            proxies: HashMap::new(),
        }
    }

    pub async fn create_proxy(
        &mut self,
        name: &str,
        provider: &str,
        spec: &HashMap<String, String>,
    ) -> Result<Arc<RwLock<ResourceProxy>>, Error> {
        if self.proxies.contains_key(name) {
            debug!("found existing proxy for resource \"{}\"", name);
            Ok(self.proxies.get(name).unwrap().clone())
        } else {
            debug!("creating new proxy for resource \"{}\"", name);
            let proxy = Arc::new(RwLock::new(ResourceProxy::new(
                name,
                self.dir.clone(),
                ResourceProvider::deserialize(provider, spec).context(ProviderError)?,
            )));
            self.proxies.insert(name.to_owned(), proxy.clone());
            Ok(proxy)
        }
    }
}

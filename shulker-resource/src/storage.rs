use snafu::{ResultExt, Snafu};
use std::collections::HashMap;
use std::path::PathBuf;
use std::sync::{Arc, RwLock};
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
    proxies: Vec<Arc<RwLock<ResourceProxy>>>,
}

impl ResourceStorage {
    pub fn new(dir: &str) -> Self {
        ResourceStorage {
            dir: PathBuf::from(dir),
            proxies: Vec::new(),
        }
    }

    pub fn push(
        &mut self,
        name: &str,
        provider: &str,
        spec: &HashMap<String, String>,
    ) -> Result<Arc<RwLock<ResourceProxy>>, Error> {
        let existing = self
            .proxies
            .iter()
            .position(|p| p.read().unwrap().name == name);
        if existing.is_some() {
            debug!("found existing proxy for resource \"{}\"", name);
            Ok(self.proxies.get(existing.unwrap()).unwrap().clone())
        } else {
            debug!("creating new proxy for resource \"{}\"", name);
            let proxy = Arc::new(RwLock::new(ResourceProxy::new(
                name,
                self.dir.clone(),
                ResourceProvider::deserialize(provider, spec).context(ProviderError)?,
            )));
            self.proxies.push(proxy.clone());
            Ok(proxy)
        }
    }
}

use futures::future;
use snafu::{ResultExt, Snafu};
use std::collections::HashMap;
use std::path::PathBuf;
use tracing::{debug, info};

use crate::provider::{self, ResourceProvider};
use crate::proxy::{self, ResourceProxy};

#[derive(Debug, Snafu)]
pub enum Error {
    ProxyError { source: proxy::Error },
    ProviderError { source: provider::Error },
}

pub struct ResourceStorage {
    dir: PathBuf,
    proxies: Vec<ResourceProxy>,
}

impl ResourceStorage {
    pub fn new(dir: &str) -> Self {
        ResourceStorage {
            dir: PathBuf::from(dir),
            proxies: Vec::new(),
        }
    }

    pub async fn sync(&mut self) -> Result<(), Error> {
        info!("syncing resource proxies");
        match future::try_join_all(self.proxies.iter_mut().map(|p| p.fetch())).await {
            Ok(_) => {
                info!("synced resource proxies");
                Ok(())
            }
            Err(error) => Err(Error::ProxyError { source: error }),
        }
    }

    pub async fn push(
        &mut self,
        name: &str,
        provider: &str,
        spec: &HashMap<String, String>,
    ) -> Result<(), Error> {
        if self.proxies.iter().any(|p| p.name == name) {
            debug!("found existing proxy for resource \"{}\"", name);
            Ok(())
        } else {
            debug!("creating new proxy for resource \"{}\"", name);
            let mut proxy = ResourceProxy::new(
                name,
                self.dir.clone(),
                ResourceProvider::deserialize(provider, spec).context(ProviderError)?,
            );

            proxy.fetch().await.context(ProxyError)?;
            self.proxies.push(proxy);
            Ok(())
        }
    }
}

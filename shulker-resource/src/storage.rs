use snafu::{ResultExt, Snafu};
use std::collections::HashMap;
use std::path::PathBuf;
use std::sync::Arc;
use tokio::sync::RwLock;
use tracing::debug;

use crate::provider::{self, ResourceProvider};
use crate::proxy::ResourceProxy;

/// Enumeration of possible errors concerning the
/// storage of resource proxies.
#[derive(Debug, Snafu)]
pub enum Error {
    /// A resource provider failed to operate correctly.
    ProviderError { source: provider::Error },
}

/// Represent a resource storage.
///
/// Only the storage is allowed to create proxies.
/// It keeps a list of created proxy to prevent
/// duplication, aka. ensure that a resource is
/// managed by only one proxy.
pub struct ResourceStorage {
    /// Root directory of the storage.
    pub(crate) dir: PathBuf,
    /// Vector of created proxies.
    proxies: HashMap<String, Arc<RwLock<ResourceProxy>>>,
}

impl ResourceStorage {
    /// Creates a new ResourceStorage with a
    /// root directory.
    ///
    /// # Arguments
    ///
    /// - `dir` - Root directory of the storage
    pub fn new(dir: &str) -> Self {
        ResourceStorage {
            dir: PathBuf::from(dir),
            proxies: HashMap::new(),
        }
    }

    /// Creates a new proxy stored on the current
    /// ResourceStorage.
    ///
    /// Will search for an existing proxy for the given
    /// resource and, if found, will return the existing
    /// one. Otherwise, it will create a new proxy from
    /// a new provider.
    ///
    /// # Arguments
    ///
    /// - `name` - Name of the resource
    /// - `provider` - Provider of the resource
    /// - `spec` - Spec applicable to the provider of the resource
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

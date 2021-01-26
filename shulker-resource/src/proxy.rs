use crate::provider::{self, ResourceProvider};
use snafu::{ResultExt, Snafu};
use std::fs::{create_dir_all, File};
use std::path::PathBuf;
use tracing::debug;

/// Enumeration of possible errors concerning the
/// synchronization of resources.
#[derive(Debug, Snafu)]
pub enum Error {
    /// A resource provider failed to operate correctly.
    ProviderError { source: provider::Error },
    /// The filesystem raised an error when writing or
    /// reading a resource.
    IOError { source: std::io::Error },
}

/// Represent a resource proxy, aka. a bridge
/// between a resource and its cached data on
/// disk.
///
/// The proxy is responsible of fetching the
/// resource if the resource cached on disk is
/// dirty (either if it doesn't exist or if the
/// remote hash if different from the local one).
pub struct ResourceProxy {
    /// Name of the resource
    pub name: String,
    /// Parent directory of the resource.
    dir: PathBuf,
    /// Provider to use to fetch the resource.
    provider: ResourceProvider,
    /// Hash of the cached data.
    local_hash: Option<String>,
    /// Hash of the remote data.
    remote_hash: Option<String>,
    /// Path to the cached data.
    pub path: Option<PathBuf>,
}

impl ResourceProxy {
    /// Creates a new ResourceProxy wrapping
    /// a given provider.
    ///
    /// # Arguments
    /// - `name` - Name of the resource
    /// - `dir` - Parent directory
    /// - `provider` - Provider of the resource
    pub fn new(name: &str, dir: PathBuf, provider: ResourceProvider) -> Self {
        ResourceProxy {
            name: name.to_owned(),
            dir,
            provider,
            local_hash: None,
            remote_hash: None,
            path: None,
        }
    }

    /// Fetch a resource behind the proxy.
    ///
    /// It will update the remote hash, check if
    /// the cached data is up-to-date and, if
    /// needed, fetch the updated resource.
    pub async fn fetch(&mut self) -> Result<(), Error> {
        debug!("updating remote hash for resource proxy \"{}\"", self.name);
        self.update_remote_hash().await?;

        if !self.is_fetch_needed() {
            Ok(())
        } else {
            debug!("fetching outdated resource proxy \"{}\"", self.name);
            create_dir_all(self.path.as_ref().unwrap().parent().unwrap()).context(IOError)?;
            let mut file = File::create(self.path.as_ref().unwrap()).context(IOError)?;
            self.provider
                .fetch(&mut file)
                .await
                .context(ProviderError)?;

            self.local_hash = self.remote_hash.clone();
            debug!(
                "fetched resource proxy \"{}\" local hash is {}",
                self.name,
                self.local_hash.as_ref().unwrap()
            );
            Ok(())
        }
    }

    /// Update the remote hash of the resource.
    ///
    /// It will retreive the remote hash from the
    /// provider and will check if a cached data
    /// matches the remote hash.
    async fn update_remote_hash(&mut self) -> Result<(), Error> {
        self.remote_hash = Some(
            self.provider
                .get_remote_hash()
                .await
                .context(ProviderError)?,
        );
        debug!(
            "updated remote hash {} for resource proxy \"{}\"",
            self.remote_hash.as_ref().unwrap(),
            self.name
        );

        let mut local_path = self.dir.clone();
        local_path.push(self.provider.get_provider_name());
        local_path.push(self.remote_hash.as_ref().unwrap());
        self.path = Some(local_path.clone());

        debug!(
            "updated local path {} for resource proxy \"{}\"",
            self.path.as_ref().unwrap().to_str().unwrap(),
            self.name
        );

        if local_path.exists() {
            self.local_hash = self.remote_hash.clone();
            debug!(
                "resource proxy \"{}\" already satisfied with local hash {}",
                self.name,
                self.local_hash.as_ref().unwrap()
            );
        }

        Ok(())
    }

    /// Determine if the resource should be fetched.
    ///
    /// Returns true when the proxy is newly created
    /// (aka. the local hash and path is unknown) or
    /// if the remote hash differ from the local one.
    fn is_fetch_needed(&self) -> bool {
        if None == self.local_hash || None == self.path {
            true
        } else {
            debug!(
                "resource proxy \"{}\" has local hash {}",
                self.name,
                self.local_hash.as_ref().unwrap()
            );
            self.remote_hash.as_ref().unwrap() != self.local_hash.as_ref().unwrap()
        }
    }
}

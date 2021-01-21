use crate::provider::ResourceProvider;
use snafu::{ResultExt, Snafu};
use std::fs::{create_dir_all, File};
use std::path::PathBuf;
use tracing::debug;

#[derive(Debug, Snafu)]
pub enum Error {
    SyncError { source: Box<dyn std::error::Error> },
    IOError { source: std::io::Error },
}

pub struct ResourceProxy {
    pub name: String,
    dir: PathBuf,
    provider: ResourceProvider,
    local_hash: Option<String>,
    remote_hash: Option<String>,
    pub path: Option<PathBuf>,
}

impl ResourceProxy {
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

    pub async fn fetch(&mut self) -> Result<(), Error> {
        debug!("updating remote hash for resource proxy \"{}\"", self.name);
        self.update_remote_hash().await?;

        if !self.is_fetch_needed() {
            Ok(())
        } else {
            debug!("fetching outdated resource proxy \"{}\"", self.name);
            create_dir_all(self.path.as_ref().unwrap().parent().unwrap()).context(IOError)?;
            let mut file = File::create(self.path.as_ref().unwrap()).context(IOError)?;
            let res = self.provider.fetch(&mut file).await;

            match res {
                Ok(_) => {
                    self.local_hash = self.remote_hash.clone();
                    debug!(
                        "fetched resource proxy \"{}\" local hash is {}",
                        self.name,
                        self.local_hash.as_ref().unwrap()
                    );
                    Ok(())
                }
                Err(error) => Err(Error::SyncError {
                    source: Box::new(error),
                }),
            }
        }
    }

    async fn update_remote_hash(&mut self) -> Result<(), Error> {
        let res = self.provider.get_remote_hash().await;

        match res {
            Ok(remote_hash) => {
                self.remote_hash = Some(remote_hash);
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
            Err(error) => Err(Error::SyncError {
                source: Box::new(error),
            }),
        }
    }

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

impl PartialEq for ResourceProxy {
    fn eq(&self, other: &ResourceProxy) -> bool {
        self.path == other.path
    }
}

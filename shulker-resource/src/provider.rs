use snafu::Snafu;
use std::collections::HashMap;
use std::fs::File;

use crate::providers;

#[derive(Debug, Snafu)]
pub enum Error {
    #[snafu(display("Unknown resource provider {}", provider))]
    UnknownProvider { provider: String },
    #[snafu(display(
        "Failed to deserialize resource provider {} spec: {}",
        provider,
        source
    ))]
    DeserializationFailed {
        provider: String,
        source: Box<dyn std::error::Error>,
    },
    #[snafu(display(
        "Resource provider {} failed to check for a cache hit: {}",
        provider,
        source
    ))]
    CheckCacheHitFailed {
        provider: &'static str,
        source: Box<dyn std::error::Error>,
    },
    #[snafu(display(
        "Resource provider {} failed to fetch a resource: {}",
        provider,
        source
    ))]
    FetchFailed {
        provider: &'static str,
        source: Box<dyn std::error::Error>,
    },
}

pub enum ResourceProvider {
    Url(providers::url::UrlResourceProvider),
    // Maven(),
}

impl ResourceProvider {
    pub fn deserialize(provider: &str, spec: &HashMap<String, String>) -> Result<Self, Error> {
        let res = match provider {
            "url" => providers::url::UrlResourceProvider::deserialize(spec),
            _ => {
                return Err(Error::UnknownProvider {
                    provider: provider.to_owned(),
                })
            }
        };

        match res {
            Ok(provider) => Ok(provider),
            Err(error) => Err(Error::DeserializationFailed {
                provider: provider.to_owned(),
                source: Box::new(error),
            }),
        }
    }

    pub async fn fetch(&self, file: &mut File) -> Result<(), Error> {
        let res = match self {
            ResourceProvider::Url(provider) => provider.fetch(file).await,
        };

        match res {
            Ok(()) => Ok(()),
            Err(error) => Err(Error::FetchFailed {
                provider: self.get_provider_name(),
                source: Box::new(error),
            }),
        }
    }

    pub async fn get_remote_hash(&self) -> Result<String, Error> {
        let res = match self {
            ResourceProvider::Url(provider) => provider.get_remote_hash().await,
        };

        match res {
            Ok(hit_result) => Ok(hit_result.trim().to_owned()),
            Err(error) => Err(Error::CheckCacheHitFailed {
                provider: self.get_provider_name(),
                source: Box::new(error),
            }),
        }
    }

    pub fn get_provider_name(&self) -> &'static str {
        match self {
            ResourceProvider::Url(_) => "url",
            // ResourceProvider::Maven() => "maven"
        }
    }
}

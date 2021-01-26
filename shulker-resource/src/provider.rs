use snafu::Snafu;
use std::collections::HashMap;
use std::fs::File;

use crate::providers;

/// Enumeration of possible errors concerning the
/// life management of a resource provider.
#[derive(Debug, Snafu)]
pub enum Error {
    /// One tried to deserialize an unknown provider.
    #[snafu(display("Unknown resource provider {}", provider))]
    UnknownProvider { provider: String },
    /// The resource provider implementation failed to
    /// deserialize its spec.
    #[snafu(display(
        "Failed to deserialize resource provider {} spec: {}",
        provider,
        source
    ))]
    DeserializationFailed {
        provider: String,
        source: Box<dyn std::error::Error>,
    },
    /// The resource provider implementation failed to
    /// fetch a resource.
    #[snafu(display(
        "Resource provider {} failed to fetch a resource: {}",
        provider,
        source
    ))]
    FetchFailed {
        provider: &'static str,
        source: Box<dyn std::error::Error>,
    },
    /// The resource provider implementation failed to
    /// retrieve the remote hash of a resource.
    #[snafu(display(
        "Resource provider {} failed to retrieve a remote hash: {}",
        provider,
        source
    ))]
    RetrieveRemoteHashFailed {
        provider: &'static str,
        source: Box<dyn std::error::Error>,
    },
}

/// Enumeration of supported resource providers.
pub enum ResourceProvider {
    Url(providers::url::UrlResourceProvider),
    // Maven(),
}

impl ResourceProvider {
    /// Deserializes a resource provider from its
    /// name and spec.
    ///
    /// The provider will validate the spec upon
    /// creation.
    ///
    /// # Arguments
    /// - `provider` - Name of the resource provider
    /// - `spec` - Spec of the resource provider
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

    /// Fetch a resource and write it to the given
    /// file.
    ///
    /// # Arguments
    /// - `file` - Opened file to write the resource in
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

    /// Retrieve the remote hash of the resource.
    pub async fn get_remote_hash(&self) -> Result<String, Error> {
        let res = match self {
            ResourceProvider::Url(provider) => provider.get_remote_hash().await,
        };

        match res {
            Ok(hit_result) => Ok(hit_result.trim().to_owned()),
            Err(error) => Err(Error::RetrieveRemoteHashFailed {
                provider: self.get_provider_name(),
                source: Box::new(error),
            }),
        }
    }

    /// Get the name of the current provider.
    pub fn get_provider_name(&self) -> &'static str {
        match self {
            ResourceProvider::Url(_) => "url",
            // ResourceProvider::Maven() => "maven"
        }
    }
}

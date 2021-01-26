use snafu::{ensure, ResultExt, Snafu};
use std::collections::HashMap;
use std::fs::File;

use crate::provider::ResourceProvider;

/// Enumeration of possible errors concerning the
/// life management of a UrlResourceProvider.
#[derive(Debug, Snafu)]
pub enum Error {
    /// The spec is malformed.
    #[snafu(display("Malformed spec for resource provider url: {}", reason))]
    MalformedSpec { reason: String },
    /// The resource was not found of the file
    /// server.
    NotFoundError,
    /// The request was successfully sent and the
    /// server responsed something other then 200 OK.
    #[snafu(display(
        "Failed to perform request to url {}, server returned {} status",
        url,
        status
    ))]
    NetworkError { url: String, status: u16 },
    /// Reqwest failed to perform the request.
    ReqwestError { source: reqwest::Error },
    /// A filesystem error was raised during the copy
    /// of the resource.
    IOError { source: std::io::Error },
}

/// Implementation of a url-based resource provider.
///
/// The resource is identified with a URL pointing on a
/// file server. The remote hash is retrieved by downloading
/// the same URL with `.sha1` at the end, a file containing
/// the hash of the resource itself.
///
/// This implementation uses the `reqwest` crate to
/// perform the HTTP requests.
pub struct UrlResourceProvider {
    /// Url of the resource.
    url: String,
}

impl UrlResourceProvider {
    /// Deserialize a UrlResourceProvider from a spec.
    ///
    /// # Arguments
    /// - `spec` - Resource provider spec
    pub fn deserialize(spec: &HashMap<String, String>) -> Result<ResourceProvider, Error> {
        UrlResourceProvider::validate_spec(spec)?;

        Ok(ResourceProvider::Url(UrlResourceProvider {
            url: spec.get("url").unwrap().clone(),
        }))
    }

    /// Fetch a resource and write it to the given
    /// file.
    ///
    /// # Arguments
    /// - `file` - Opened file to write the resource in
    pub async fn fetch(&self, file: &mut File) -> Result<(), Error> {
        let response = reqwest::get(&self.url).await.context(ReqwestError)?;

        match response.status() {
            reqwest::StatusCode::OK => {
                let response_content = response.text().await.context(ReqwestError)?;
                std::io::copy(&mut response_content.as_bytes(), file).context(IOError)?;
                Ok(())
            }
            reqwest::StatusCode::NOT_FOUND => Err(Error::NotFoundError),
            status => Err(Error::NetworkError {
                url: self.url.to_owned(),
                status: status.as_u16(),
            }),
        }
    }

    /// Retrieve the remote hash of the resource.
    pub async fn get_remote_hash(&self) -> Result<String, Error> {
        let url = format!("{}.sha1", self.url);
        let response = reqwest::get(&url).await.context(ReqwestError)?;

        match response.status() {
            reqwest::StatusCode::OK => response.text().await.context(ReqwestError),
            reqwest::StatusCode::NOT_FOUND => Err(Error::NotFoundError),
            status => Err(Error::NetworkError {
                url: url.clone(),
                status: status.as_u16(),
            }),
        }
    }

    /// Validate the spec of a UrlResourceProvider.
    ///
    /// It must contains a URL pointing to the resource.
    fn validate_spec(spec: &HashMap<String, String>) -> Result<(), Error> {
        ensure!(
            spec.contains_key("url"),
            MalformedSpec {
                reason: "malformed url".to_owned(),
            }
        );

        Ok(())
    }
}

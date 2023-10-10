use super::{ResourceRefError, Result};

#[derive(Clone, Debug, PartialEq)]
pub struct HttpCredentials {
    pub username: String,
    pub password: String,
}

impl HttpCredentials {
    pub fn from_kubernetes_secret(
        secret: &k8s_openapi::api::core::v1::Secret,
    ) -> Result<HttpCredentials> {
        match &secret.data {
            None => Err(ResourceRefError::InvalidSecretSpec(
                "Secret data is empty".to_string(),
            )),
            Some(secret_data) => {
                let username_opt = secret_data
                    .get("username")
                    .map(|bs| std::str::from_utf8(&bs.0).unwrap());
                let password_opt = secret_data
                    .get("password")
                    .map(|bs| std::str::from_utf8(&bs.0).unwrap());

                if let (Some(username), Some(password)) = (username_opt, password_opt) {
                    Ok(HttpCredentials {
                        username: username.to_string(),
                        password: password.to_string(),
                    })
                } else {
                    Err(ResourceRefError::InvalidSecretSpec(
                        "Missing username or password key in secret".to_string(),
                    ))
                }
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use std::collections::BTreeMap;

    use k8s_openapi::api::core::v1::Secret;

    use crate::resources::http_credentials::HttpCredentials;

    #[test]
    fn parse_successfully() {
        // G
        let secret = Secret {
            data: Some(BTreeMap::from([
                (
                    "username".to_string(),
                    k8s_openapi::ByteString("user".as_bytes().to_vec()),
                ),
                (
                    "password".to_string(),
                    k8s_openapi::ByteString("pass".as_bytes().to_vec()),
                ),
            ])),
            ..Secret::default()
        };

        // W
        let http_credentials = HttpCredentials::from_kubernetes_secret(&secret).unwrap();

        // T
        assert_eq!(
            http_credentials,
            HttpCredentials {
                username: "user".to_string(),
                password: "pass".to_string()
            }
        );
    }

    #[test]
    #[should_panic(expected = "Missing username or password key in secret")]
    fn fail_without_username() {
        // G
        let secret = Secret {
            data: Some(BTreeMap::from([(
                "password".to_string(),
                k8s_openapi::ByteString("pass".as_bytes().to_vec()),
            )])),
            ..Secret::default()
        };

        // W
        HttpCredentials::from_kubernetes_secret(&secret).unwrap();
    }

    #[test]
    #[should_panic(expected = "Missing username or password key in secret")]
    fn fail_without_password() {
        // G
        let secret = Secret {
            data: Some(BTreeMap::from([(
                "username".to_string(),
                k8s_openapi::ByteString("user".as_bytes().to_vec()),
            )])),
            ..Secret::default()
        };

        // W
        HttpCredentials::from_kubernetes_secret(&secret).unwrap();
    }
}

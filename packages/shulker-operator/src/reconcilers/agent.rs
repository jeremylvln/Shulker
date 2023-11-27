use shulker_crds::resourceref::{ResourceRefFromMavenSpec, ResourceRefFromSpec, ResourceRefSpec};
use url::Url;

use crate::{
    agent::AgentConfig,
    resources::{resourceref_resolver::ResourceRefResolver, ResourceRefError},
};

pub enum AgentSide {
    Proxy,
    Server,
}

impl AgentSide {
    pub fn get_artifact_name(&self) -> &'static str {
        match self {
            AgentSide::Proxy => "shulker-proxy-agent",
            AgentSide::Server => "shulker-server-agent",
        }
    }
}

pub async fn get_agent_plugin_url(
    resourceref_resolver: &ResourceRefResolver,
    agent_config: &AgentConfig,
    side: AgentSide,
    platform: String,
) -> Result<Url, ResourceRefError> {
    resourceref_resolver
        .resolve(
            "shulker-system",
            &ResourceRefSpec {
                url_from: Some(ResourceRefFromSpec {
                    maven_ref: Some(ResourceRefFromMavenSpec {
                        repository_url: agent_config.maven_repository.clone(),
                        group_id: "io.shulkermc".to_string(),
                        artifact_id: side.get_artifact_name().to_string(),
                        version: agent_config.version.clone(),
                        classifier: Some(platform),
                        credentials_secret_name: None,
                    }),
                }),
                ..ResourceRefSpec::default()
            },
        )
        .await?
        .as_url()
}

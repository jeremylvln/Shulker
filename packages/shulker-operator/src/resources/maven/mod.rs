use serde::Deserialize;

pub mod resolver;

#[cfg(test)]
mod fixtures;

#[derive(Deserialize, Clone, Debug, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct MavenMetadata {
    pub versioning: MavenMetadataVersionning,
}

#[derive(Deserialize, Clone, Debug, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct MavenMetadataVersionning {
    pub snapshot_versions: Option<MavenMetadataVersionningSnapshotVersions>,
}

#[derive(Deserialize, Clone, Debug, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct MavenMetadataVersionningSnapshotVersions {
    #[serde(rename = "#content")]
    pub versions: Vec<MavenMetadataVersionningSnapshotVersion>,
}

#[derive(Deserialize, Clone, Debug, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct MavenMetadataVersionningSnapshotVersion {
    pub extension: String,
    pub value: String,
    pub updated: u64,
    pub classifier: Option<String>,
}

impl MavenMetadata {
    pub fn find_latest_snapshot_version(&self, extension: &str) -> Option<String> {
        self.find_latest_snapshot_version_for_classifier(extension, None)
    }

    pub fn find_latest_snapshot_version_for_classifier(
        &self,
        extension: &str,
        classifier: Option<&str>,
    ) -> Option<String> {
        match &self.versioning.snapshot_versions {
            Some(versions) => {
                let mut versions = versions.versions.clone();
                versions.sort_by(|a, b| b.updated.cmp(&a.updated));
                versions
                    .into_iter()
                    .find(|v| v.extension == extension && v.classifier.as_deref() == classifier)
                    .map(|v| v.value)
            }
            None => None,
        }
    }
}

#[cfg(test)]
mod tests {
    use crate::resources::maven::{
        MavenMetadata, MavenMetadataVersionning, MavenMetadataVersionningSnapshotVersion,
        MavenMetadataVersionningSnapshotVersions,
    };

    use super::fixtures::MAVEN_METADATA;

    #[test]
    fn parse() {
        // G
        let metadata: MavenMetadata = serde_xml_rs::from_str(MAVEN_METADATA).unwrap();

        // W
        assert_eq!(
            metadata,
            MavenMetadata {
                versioning: MavenMetadataVersionning {
                    snapshot_versions: Some(MavenMetadataVersionningSnapshotVersions {
                        versions: vec![
                            MavenMetadataVersionningSnapshotVersion {
                                extension: "jar".to_string(),
                                value: "0.3.0-20231127.141358-1".to_string(),
                                updated: 20231127141358,
                                classifier: Some("javadoc".to_string()),
                            },
                            MavenMetadataVersionningSnapshotVersion {
                                extension: "jar".to_string(),
                                value: "0.3.0-20231127.141358-2".to_string(),
                                updated: 20231127141358,
                                classifier: Some("sources".to_string()),
                            },
                            MavenMetadataVersionningSnapshotVersion {
                                extension: "jar".to_string(),
                                value: "0.3.0-20231127.141358-3".to_string(),
                                updated: 20231127141358,
                                classifier: None,
                            },
                        ],
                    }),
                }
            }
        )
    }

    #[test]
    fn find_latest_snapshot_version() {
        // G
        let metadata: MavenMetadata = serde_xml_rs::from_str(MAVEN_METADATA).unwrap();

        // W
        let version = metadata.find_latest_snapshot_version("jar");

        // T
        assert_eq!(version, Some("0.3.0-20231127.141358-3".to_string()))
    }

    #[test]
    fn find_latest_snapshot_version_for_classifier() {
        // G
        let metadata: MavenMetadata = serde_xml_rs::from_str(MAVEN_METADATA).unwrap();

        // W
        let version = metadata.find_latest_snapshot_version_for_classifier("jar", Some("sources"));

        // T
        assert_eq!(version, Some("0.3.0-20231127.141358-2".to_string()))
    }
}

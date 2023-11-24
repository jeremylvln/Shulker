#[cfg(any(test, debug_assertions))]
pub const SHULKER_PLUGIN_REPOSITORY: &str =
    "https://maven.jeremylvln.fr/artifactory/shulker-snapshots";
#[cfg(not(debug_assertions))]
pub const SHULKER_PLUGIN_REPOSITORY: &str =
    "https://maven.jeremylvln.fr/artifactory/shulker-releases";

#[cfg(test)]
pub const SHULKER_PLUGIN_VERSION: &str = "0.0.0-test-cfg";
#[cfg(all(not(test), debug_assertions))]
pub const SHULKER_PLUGIN_VERSION: &str =
    const_format::concatcp!(env!("CARGO_PKG_VERSION"), "-SNAPSHOT");
#[cfg(not(debug_assertions))]
pub const SHULKER_PLUGIN_VERSION: &str = env!("CARGO_PKG_VERSION");

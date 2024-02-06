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

pub const PROXY_IMAGE: &str = "itzg/bungeecord:java17-2024.2.0";
pub const MINECRAFT_SERVER_IMAGE: &str = "itzg/minecraft-server:2024.2.1-java17";

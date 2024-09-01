#[cfg(any(test, debug_assertions))]
pub const SHULKER_PLUGIN_REPOSITORY: &str =
    "https://maven.jeremylvln.fr/repository/shulker-snapshots";
#[cfg(not(debug_assertions))]
pub const SHULKER_PLUGIN_REPOSITORY: &str =
    "https://maven.jeremylvln.fr/repository/shulker-releases";

#[cfg(test)]
pub const SHULKER_PLUGIN_VERSION: &str = "0.0.0-test-cfg";
#[cfg(all(not(test), debug_assertions))]
pub const SHULKER_PLUGIN_VERSION: &str =
    const_format::concatcp!(env!("CARGO_PKG_VERSION"), "-SNAPSHOT");
#[cfg(not(debug_assertions))]
pub const SHULKER_PLUGIN_VERSION: &str = env!("CARGO_PKG_VERSION");

pub const PROXY_IMAGE: &str = "itzg/mc-proxy:2024.6.0-java21";
pub const MINECRAFT_SERVER_IMAGE: &str = "itzg/minecraft-server:2024.7.2-java21";

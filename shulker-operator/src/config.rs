use envconfig::Envconfig;

/// Configuration extracted from the
/// environment.
#[derive(Envconfig, Clone)]
pub struct Config {
    /// Directory to use as resource storage
    /// root.
    #[envconfig(from = "SHULKER_CACHE_DIR", default = "cache")]
    pub cache_dir: String,
}

use envconfig::Envconfig;

#[derive(Envconfig, Clone)]
pub struct Config {
    #[envconfig(from = "SHULKER_CACHE_DIR", default = "cache")]
    pub cache_dir: String,
}

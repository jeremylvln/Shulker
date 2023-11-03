use tracing_subscriber::{prelude::*, EnvFilter, Registry};

pub async fn init() {
    #[cfg(not(debug_assertions))]
    let logger = tracing_subscriber::fmt::layer().json();
    #[cfg(debug_assertions)]
    let logger = tracing_subscriber::fmt::layer().compact();

    let env_filter = EnvFilter::try_from_default_env()
        .or(EnvFilter::try_new("info"))
        .unwrap();

    let collector = Registry::default().with(logger).with(env_filter);
    tracing::subscriber::set_global_default(collector).unwrap();
}

/// Resource provider abstraction.
pub(crate) mod provider;
/// Resource provider implementations.
pub(crate) mod providers;
/// Proxy between a resource provider and
/// its cached data.
pub mod proxy;
/// Resource proxy storage.
pub mod storage;

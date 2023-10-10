use chrono::{DateTime, Utc};

#[cfg(not(feature = "testing"))]
pub fn now() -> DateTime<Utc> {
    Utc::now()
}

#[cfg(feature = "testing")]
thread_local! {
    pub static UTC_TIME_SECONDS: std::sync::atomic::AtomicI64 = std::sync::atomic::AtomicI64::new(0);
}

#[cfg(feature = "testing")]
pub fn now() -> DateTime<Utc> {
    let seconds = UTC_TIME_SECONDS.with(|x| x.load(std::sync::atomic::Ordering::SeqCst));
    DateTime::from_timestamp(seconds, 0).unwrap()
}

#[cfg(feature = "testing")]
pub fn set_test_time_seconds(seconds: i64) {
    UTC_TIME_SECONDS.with(|x| x.store(seconds, std::sync::atomic::Ordering::SeqCst));
}

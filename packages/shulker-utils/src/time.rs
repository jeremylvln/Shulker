use chrono::{DateTime, Utc};

#[cfg(not(feature = "testing"))]
pub fn now() -> DateTime<Utc> {
    Utc::now()
}

#[cfg(feature = "testing")]
thread_local! {
    pub static UTC_TIME_SECONDS: std::sync::atomic::AtomicI64 = const { std::sync::atomic::AtomicI64::new(0) };
}

#[cfg(feature = "testing")]
pub fn now() -> DateTime<Utc> {
    use chrono::TimeZone;

    let seconds = UTC_TIME_SECONDS.with(|x| x.load(std::sync::atomic::Ordering::SeqCst));
    Utc.timestamp_opt(seconds, 0).unwrap()
}

#[cfg(feature = "testing")]
pub fn set_test_time_seconds(seconds: i64) {
    UTC_TIME_SECONDS.with(|x| x.store(seconds, std::sync::atomic::Ordering::SeqCst));
}

#[cfg(not(feature = "testing"))]
mod tests {
    #[test]
    fn now_should_return_current_time() {
        // W
        let time = super::now();

        // T
        assert!(time.timestamp() > 0);
    }
}

#[cfg(feature = "testing")]
mod tests {
    #[test]
    fn now_should_return_default_time() {
        // W
        let time = super::now();

        // T
        assert_eq!(time.timestamp(), 0);
    }

    #[test]
    fn now_should_return_defined_time() {
        // G
        super::set_test_time_seconds(42);

        // W
        let time = super::now();

        // T
        assert_eq!(time.timestamp(), 42);
    }
}

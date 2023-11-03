use chrono::Duration;
use k8s_openapi::{
    api::coordination::v1::{Lease, LeaseSpec},
    apimachinery::pkg::apis::meta::v1::MicroTime,
};
use kube::{
    api::{Patch, PatchParams, PostParams},
    core::ObjectMeta,
    Api,
};
use tracing::*;

use shulker_utils::time;

const LEASE_DURATION_SECONDS: u64 = 15;
const LEASE_RENEW_INTERVAL_SECONDS: u64 = 10;

pub struct LeaseLock {
    leases_api: Api<Lease>,
    namespace: String,
    name: String,
    holder_identity: String,
    owned_patch_params: PatchParams,
    current_resource_version: Option<String>,
}

impl LeaseLock {
    fn new(
        client: kube::Client,
        name: String,
        controller_name: String,
    ) -> Result<Self, anyhow::Error> {
        let leases_api: Api<Lease> =
            kube::Api::namespaced(client.clone(), client.default_namespace());

        let holder_identity = LeaseLock::get_local_identity()?;
        let owned_patch_params = PatchParams::apply(&controller_name).force();

        Ok(LeaseLock {
            leases_api,
            namespace: client.default_namespace().to_string(),
            name,
            holder_identity,
            owned_patch_params,
            current_resource_version: None,
        })
    }

    async fn try_acquire(&mut self) -> Result<(), anyhow::Error> {
        info!(
            namespace = self.namespace,
            name = self.name,
            "acquiring lease"
        );

        let lease = loop {
            let lease = self.leases_api.get(&self.name).await;

            if let Err(kube::Error::Api(kube::error::ErrorResponse { code: 404, .. })) = lease {
                trace!("lease does not exist, creating new one");
                let lease = self
                    .leases_api
                    .create(
                        &PostParams::default(),
                        &Lease {
                            metadata: ObjectMeta {
                                namespace: Some(self.namespace.to_string()),
                                name: Some(self.name.to_string()),
                                ..Default::default()
                            },
                            spec: Some(LeaseSpec {
                                acquire_time: Some(MicroTime(time::now())),
                                lease_duration_seconds: Some(LEASE_DURATION_SECONDS as i32),
                                holder_identity: Some(self.holder_identity.to_string()),
                                lease_transitions: Some(1),
                                ..Default::default()
                            }),
                        },
                    )
                    .await
                    .unwrap();

                break lease;
            } else if let Ok(mut lease) = lease {
                if LeaseLock::is_expired(&lease) {
                    trace!("lease is expired, trying to take ownership");
                    lease.metadata.managed_fields = None;

                    let spec = lease.spec.as_mut().unwrap();
                    if spec.lease_transitions.is_none() {
                        spec.lease_transitions = Some(0);
                    }
                    spec.lease_transitions = spec.lease_transitions.map(|lt| lt + 1);
                    spec.acquire_time = Some(MicroTime(time::now()));
                    spec.renew_time = None;
                    spec.lease_duration_seconds = Some(LEASE_DURATION_SECONDS as i32);
                    spec.holder_identity = Some(self.holder_identity.to_string());

                    let lease = self
                        .leases_api
                        .patch(&self.name, &self.owned_patch_params, &Patch::Apply(lease))
                        .await
                        .unwrap();

                    break lease;
                } else {
                    let wait_time = match lease.spec {
                        Some(LeaseSpec {
                            lease_duration_seconds: Some(lds),
                            ..
                        }) => lds as u64,
                        _ => LEASE_DURATION_SECONDS,
                    };

                    trace!(
                        "lease is not ready, let's wait {} seconds and try again",
                        wait_time
                    );

                    let mut interval =
                        tokio::time::interval(tokio::time::Duration::from_secs(wait_time));
                    interval.tick().await;
                    interval.tick().await;

                    continue;
                }
            }

            return Err(anyhow::anyhow!("failed to acquire lease"));
        };

        self.current_resource_version = lease.metadata.resource_version.clone();
        info!(
            namespace = self.namespace,
            name = self.name,
            resource_version = self.current_resource_version,
            "acquired lease"
        );

        Ok(())
    }

    async fn try_renew(&mut self) -> Result<(), anyhow::Error> {
        trace!(
            namespace = self.namespace,
            name = self.name,
            "renewing lease"
        );
        let patch = Lease {
            metadata: ObjectMeta {
                resource_version: self.current_resource_version.clone(),
                ..Default::default()
            },
            spec: Some(LeaseSpec {
                renew_time: Some(MicroTime(time::now())),
                ..Default::default()
            }),
        };

        let lease = self
            .leases_api
            .patch(&self.name, &self.owned_patch_params, &Patch::Apply(&patch))
            .await
            .unwrap();

        self.current_resource_version = lease.metadata.resource_version.clone();
        trace!(
            namespace = self.namespace,
            name = self.name,
            resource_version = self.current_resource_version,
            "renewed lease"
        );

        Ok(())
    }

    async fn try_release(&mut self) -> Result<(), anyhow::Error> {
        trace!(
            namespace = self.namespace,
            name = self.name,
            "releasing lease"
        );
        let patch = Lease {
            metadata: ObjectMeta {
                resource_version: self.current_resource_version.clone(),
                ..Default::default()
            },
            spec: Some(LeaseSpec {
                renew_time: None,
                acquire_time: None,
                holder_identity: None,
                ..Default::default()
            }),
        };

        let lease = self
            .leases_api
            .patch(&self.name, &self.owned_patch_params, &Patch::Apply(&patch))
            .await
            .unwrap();

        self.current_resource_version = lease.metadata.resource_version.clone();
        trace!(
            namespace = self.namespace,
            name = self.name,
            resource_version = self.current_resource_version,
            "released lease"
        );

        Ok(())
    }

    fn is_expired(lease: &Lease) -> bool {
        let LeaseSpec {
            acquire_time,
            renew_time,
            lease_duration_seconds,
            ..
        } = lease.spec.as_ref().unwrap();

        let utc_now = time::now();
        let lease_duration = Duration::seconds(lease_duration_seconds.unwrap() as i64);

        if let Some(MicroTime(time)) = renew_time {
            let renew_expire = time.checked_add_signed(lease_duration).unwrap();
            return utc_now.gt(&renew_expire);
        } else if let Some(MicroTime(time)) = acquire_time {
            let acquire_expire = time.checked_add_signed(lease_duration).unwrap();
            return utc_now.gt(&acquire_expire);
        }

        true
    }

    fn get_local_identity() -> Result<String, anyhow::Error> {
        let host = hostname::get()?.into_string().unwrap();
        let unique_suffix = uuid::Uuid::new_v4();
        Ok(format!("{}-{}", host, unique_suffix))
    }
}

pub async fn try_acquire_and_hold(
    client: kube::Client,
    name: String,
    controller_name: String,
    cancellation_token: tokio_util::sync::CancellationToken,
) -> Result<tokio::task::JoinHandle<()>, anyhow::Error> {
    let mut lease = LeaseLock::new(client, name, controller_name)?;

    lease.try_acquire().await?;

    let task = tokio::spawn(async move {
        let mut interval = tokio::time::interval(tokio::time::Duration::from_secs(
            LEASE_RENEW_INTERVAL_SECONDS,
        ));
        interval.tick().await;

        loop {
            tokio::select! {
                _ = interval.tick() => {
                    lease.try_renew().await.unwrap();
                }
                _ = cancellation_token.cancelled() => {
                    break
                }
            }
        }

        lease.try_release().await.unwrap();
    });

    Ok(task)
}

#[cfg(test)]
mod tests {
    use k8s_openapi::{
        api::coordination::v1::{Lease, LeaseSpec},
        apimachinery::pkg::apis::meta::v1::MicroTime,
    };

    use shulker_utils::time;

    #[test]
    fn is_expired_empty() {
        // G
        let lease = Lease {
            spec: Some(LeaseSpec {
                acquire_time: None,
                renew_time: None,
                lease_duration_seconds: Some(15),
                ..Default::default()
            }),
            ..Lease::default()
        };

        // W
        let is_expired = super::LeaseLock::is_expired(&lease);

        // T
        assert!(is_expired);
    }

    #[test]
    fn is_expired_first_acquired_in_range() {
        // G
        time::set_test_time_seconds(0);
        let lease = Lease {
            spec: Some(LeaseSpec {
                acquire_time: Some(MicroTime(time::now())),
                renew_time: None,
                lease_duration_seconds: Some(15),
                ..Default::default()
            }),
            ..Lease::default()
        };

        // W
        let is_expired = super::LeaseLock::is_expired(&lease);

        // T
        assert!(!is_expired);
    }

    #[test]
    fn is_expired_first_acquired_out_range() {
        // G
        time::set_test_time_seconds(0);
        let lease = Lease {
            spec: Some(LeaseSpec {
                acquire_time: Some(MicroTime(time::now())),
                renew_time: None,
                lease_duration_seconds: Some(15),
                ..Default::default()
            }),
            ..Lease::default()
        };

        // W
        time::set_test_time_seconds(30);
        let is_expired = super::LeaseLock::is_expired(&lease);

        // T
        assert!(is_expired);
    }

    #[test]
    fn is_expired_renew_in_range() {
        // G
        time::set_test_time_seconds(0);
        let lease = Lease {
            spec: Some(LeaseSpec {
                acquire_time: Some(MicroTime(time::now())),
                renew_time: Some(MicroTime(time::now())),
                lease_duration_seconds: Some(15),
                ..Default::default()
            }),
            ..Lease::default()
        };

        // W
        let is_expired = super::LeaseLock::is_expired(&lease);

        // T
        assert!(!is_expired);
    }

    #[test]
    fn is_expired_renew_out_range() {
        // G
        time::set_test_time_seconds(0);
        let lease = Lease {
            spec: Some(LeaseSpec {
                acquire_time: Some(MicroTime(time::now())),
                renew_time: Some(MicroTime(time::now())),
                lease_duration_seconds: Some(15),
                ..Default::default()
            }),
            ..Lease::default()
        };

        // W
        time::set_test_time_seconds(30);
        let is_expired = super::LeaseLock::is_expired(&lease);

        // T
        assert!(is_expired);
    }

    #[test]
    fn get_local_identity_format() {
        // G
        let host = hostname::get().unwrap().into_string().unwrap();
        let prefix = format!("{}-", host);

        // W
        let local_identity = super::LeaseLock::get_local_identity().unwrap();

        // T
        assert!(
            local_identity.starts_with(&prefix),
            "local_identity = {}, prefix = {}",
            local_identity,
            prefix
        );
    }
}

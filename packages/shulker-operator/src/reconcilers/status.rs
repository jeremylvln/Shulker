use kube::{
    api::{Patch, PatchParams},
    core::object::HasStatus,
    Api, ResourceExt,
};
use serde::{de::DeserializeOwned, Serialize};

use crate::reconcilers::Result;

use super::ReconcilerError;

pub async fn patch_status<
    Resource: kube::Resource<DynamicType = ()> + DeserializeOwned + HasStatus,
>(
    api: &Api<Resource>,
    pp: &PatchParams,
    resource: &Resource,
) -> Result<()>
where
    Resource::Status: Serialize,
{
    if let Some(status) = resource.status() {
        let status_json = serde_json::json!({
            "apiVersion": Resource::api_version(&()),
            "kind": Resource::kind(&()),
            "status": status
        });

        api.patch_status(&resource.name_any(), pp, &Patch::Apply(&status_json))
            .await
            .map_err(ReconcilerError::FailedToUpdateStatus)?;
    }

    Ok(())
}

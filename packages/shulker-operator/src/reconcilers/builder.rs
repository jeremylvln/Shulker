use std::fmt::Debug;
use tracing::*;

use kube::{
    api::{DeleteParams, Patch, PatchParams, PostParams},
    Api, ResourceExt,
};
use serde::{de::DeserializeOwned, Serialize};

#[async_trait::async_trait]
pub trait ResourceBuilder {
    type OwnerType: kube::Resource<DynamicType = ()> + Clone + Serialize + DeserializeOwned + Debug;
    type ResourceType: kube::Resource<DynamicType = ()>
        + Clone
        + Serialize
        + DeserializeOwned
        + Debug;

    fn name(owner: &Self::OwnerType) -> String;
    fn is_updatable() -> bool;

    fn api(&self, owner: &Self::OwnerType) -> Api<Self::ResourceType>;
    fn is_needed(&self, owner: &Self::OwnerType) -> bool;

    async fn create(
        &self,
        owner: &Self::OwnerType,
        name: &str,
    ) -> Result<Self::ResourceType, anyhow::Error>;
    async fn update(
        &self,
        owner: &Self::OwnerType,
        resource: &mut Self::ResourceType,
    ) -> Result<(), anyhow::Error>;
}

async fn get_existing<
    R: kube::Resource<DynamicType = ()> + Clone + Serialize + DeserializeOwned + Debug,
>(
    api: &Api<R>,
    name: &str,
) -> Result<Option<R>, anyhow::Error> {
    api.get(name).await.map(|r| Some(r)).or_else(|e| match e {
        kube::Error::Api(e) => match e.code {
            404 => Ok(None),
            _ => Err(e.into()),
        },
        e => Err(e.into()),
    })
}

fn set_controller_reference<
    O: kube::Resource<DynamicType = ()> + Clone + Serialize + DeserializeOwned + Debug,
    R: kube::Resource<DynamicType = ()> + Clone + Serialize + DeserializeOwned + Debug,
>(
    owner: &O,
    resource: &mut R,
) {
    resource
        .owner_references_mut()
        .push(owner.controller_owner_ref(&()).unwrap());
}

pub async fn reconcile_builder<
    O: kube::Resource<DynamicType = ()> + Clone + Serialize + DeserializeOwned + Debug,
    R: kube::Resource<DynamicType = ()> + Clone + Serialize + DeserializeOwned + Debug,
    RB: ResourceBuilder<OwnerType = O, ResourceType = R>,
>(
    builder: &RB,
    owner: &O,
) -> super::Result<Option<R>> {
    let api = builder.api(owner);
    let name = RB::name(owner);

    debug!(
        builder = std::any::type_name::<RB>(),
        owner = std::any::type_name::<O>(),
        owner_name = owner.name_any(),
        target = std::any::type_name::<R>(),
        target_name = name,
        "reconciling builder",
    );

    let existing_resource = get_existing(&api, &name)
        .await
        .map_err(|e| super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e))?;

    if !builder.is_needed(owner) {
        if existing_resource.is_some() {
            api.delete(&name, &DeleteParams::default())
                .await
                .map_err(|e| {
                    super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e.into())
                })?;
        }

        return Ok(None);
    }

    let mut new_resource = builder
        .create(owner, &name)
        .await
        .map_err(|e| super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e))?;

    builder
        .update(owner, &mut new_resource)
        .await
        .map_err(|e| super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e))?;

    let updated_resource = if let Some(existing_resource) = existing_resource {
        if RB::is_updatable() {
            for (key, value) in existing_resource.annotations().iter() {
                if !new_resource.annotations().contains_key(key) {
                    new_resource
                        .annotations_mut()
                        .insert(key.clone(), value.clone());
                }
            }

            api.patch(
                &name,
                &PatchParams::apply("shulker-operator").force(),
                &Patch::Apply(&new_resource),
            )
            .await
            .map_err(|e| {
                super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e.into())
            })?
        } else {
            existing_resource
        }
    } else {
        set_controller_reference(owner, &mut new_resource);
        api.create(
            &PostParams {
                dry_run: false,
                field_manager: Some("shulker-operator".to_string()),
            },
            &new_resource,
        )
        .await
        .map_err(|e| super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e.into()))?
    };

    Ok(Some(updated_resource))
}

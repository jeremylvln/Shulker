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

async fn does_exists<
    R: kube::Resource<DynamicType = ()> + Clone + Serialize + DeserializeOwned + Debug,
>(
    api: &Api<R>,
    name: &str,
) -> Result<bool, anyhow::Error> {
    api.get(name).await.map(|_| true).or_else(|e| match e {
        kube::Error::Api(e) => match e.code {
            404 => Ok(false),
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

    let does_exists = does_exists(&api, &name)
        .await
        .map_err(|e| super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e))?;

    if !builder.is_needed(owner) {
        if does_exists {
            api.delete(&name, &DeleteParams::default())
                .await
                .map_err(|e| {
                    super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e.into())
                })?;
        }

        return Ok(None);
    }

    let mut resource = builder
        .create(owner, &name)
        .await
        .map_err(|e| super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e))?;

    builder
        .update(owner, &mut resource)
        .await
        .map_err(|e| super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e))?;

    let updated_resource = if RB::is_updatable() && does_exists {
        api.patch(
            &name,
            &PatchParams::apply("shulker-operator"),
            &Patch::Apply(&resource),
        )
        .await
        .map_err(|e| super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e.into()))?
    } else if !does_exists {
        set_controller_reference(owner, &mut resource);
        api.create(&PostParams::default(), &resource)
            .await
            .map_err(|e| {
                super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e.into())
            })?
    } else {
        resource
    };

    Ok(Some(updated_resource))
}

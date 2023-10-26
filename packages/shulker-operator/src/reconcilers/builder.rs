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
    fn api(&self, owner: &Self::OwnerType) -> Api<Self::ResourceType>;

    fn is_needed(&self, _owner: &Self::OwnerType) -> bool {
        true
    }

    fn is_recreation_needed(
        _owner: &Self::OwnerType,
        _existing_resource: &Self::ResourceType,
    ) -> bool {
        false
    }

    async fn build(
        &self,
        owner: &Self::OwnerType,
        name: &str,
        existing_resource: Option<&Self::ResourceType>,
    ) -> Result<Self::ResourceType, anyhow::Error>;
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

    let mut existing_resource = get_existing(&api, &name)
        .await
        .map_err(|e| super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e))?;

    if existing_resource.is_some() {
        debug!(
            builder = std::any::type_name::<RB>(),
            owner = std::any::type_name::<O>(),
            owner_name = owner.name_any(),
            target = std::any::type_name::<R>(),
            target_name = name,
            "found existing resource",
        );

        let delete_existing = || async {
            api.delete(&name, &DeleteParams::default())
                .await
                .map_err(|e| {
                    super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e.into())
                })
        };

        if !builder.is_needed(owner) {
            debug!(
                builder = std::any::type_name::<RB>(),
                owner = std::any::type_name::<O>(),
                owner_name = owner.name_any(),
                target = std::any::type_name::<R>(),
                target_name = name,
                "existing resource is not needed anymore, deleting",
            );

            delete_existing().await?;
            return Ok(None);
        } else if RB::is_recreation_needed(owner, existing_resource.as_ref().unwrap()) {
            debug!(
                builder = std::any::type_name::<RB>(),
                owner = std::any::type_name::<O>(),
                owner_name = owner.name_any(),
                target = std::any::type_name::<R>(),
                target_name = name,
                "existing resource needs to be recreated, deleting",
            );

            delete_existing().await?;
            existing_resource = None;
        }
    }

    if !builder.is_needed(owner) {
        return Ok(None);
    }

    let mut new_resource = builder
        .build(owner, &name, existing_resource.as_ref())
        .await
        .map_err(|e| super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e))?;

    let updated_resource = if let Some(existing_resource) = existing_resource {
        debug!(
            builder = std::any::type_name::<RB>(),
            owner = std::any::type_name::<O>(),
            owner_name = owner.name_any(),
            target = std::any::type_name::<R>(),
            target_name = name,
            "patching existing resource",
        );

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
        .map_err(|e| super::ReconcilerError::BuilderError(std::any::type_name::<RB>(), e.into()))?
    } else {
        debug!(
            builder = std::any::type_name::<RB>(),
            owner = std::any::type_name::<O>(),
            owner_name = owner.name_any(),
            target = std::any::type_name::<R>(),
            target_name = name,
            "creating new resource",
        );

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

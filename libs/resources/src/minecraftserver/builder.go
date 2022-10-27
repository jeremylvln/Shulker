/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
	common "github.com/iamblueslime/shulker/libs/resources/src"
	"k8s.io/apimachinery/pkg/runtime"
)

type MinecraftServerResourceBuilder struct {
	Instance *shulkermciov1alpha1.MinecraftServer
	Scheme   *runtime.Scheme
}

func (b *MinecraftServerResourceBuilder) ResourceBuilders() ([]common.ResourceBuilder, []common.ResourceBuilder) {
	builders := []common.ResourceBuilder{
		b.MinecraftServerPod(),
	}
	dirtyBuilders := []common.ResourceBuilder{}

	if b.Instance.Spec.Configuration.ExistingConfigMapName == "" {
		builders = append(builders, b.MinecraftServerConfigMap())
	}

	return builders, dirtyBuilders
}

func (b *MinecraftServerResourceBuilder) GetPodName() string {
	return b.Instance.Name
}

func (b *MinecraftServerResourceBuilder) GetConfigMapName() string {
	if b.Instance.Spec.Configuration.ExistingConfigMapName != "" {
		return b.Instance.Spec.Configuration.ExistingConfigMapName
	}
	return fmt.Sprintf("%s-config", b.Instance.Name)
}

func (b *MinecraftServerResourceBuilder) getServiceAccountName() string {
	return fmt.Sprintf("%s-server", b.Instance.Spec.ClusterRef.Name)
}

func (b *MinecraftServerResourceBuilder) getLabels() map[string]string {
	labels := map[string]string{
		"app.kubernetes.io/name":             b.Instance.Name,
		"app.kubernetes.io/component":        "minecraftserver",
		"minecraftcluster.shulkermc.io/name": b.Instance.Spec.ClusterRef.Name,
	}

	for _, ownerReference := range b.Instance.OwnerReferences {
		if *ownerReference.Controller {
			labels["app.kubernetes.io/name"] = ownerReference.Name
			labels["app.kubernetes.io/instance"] = b.Instance.Name
			labels["minecraftserverdeployment.shulkermc.io/name"] = ownerReference.Name
			break
		}
	}

	return labels
}

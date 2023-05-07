/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"context"
	"fmt"

	shulkermciov1alpha1 "github.com/jeremylvln/shulker/packages/shulker-crds/v1alpha1"
	common "github.com/jeremylvln/shulker/packages/shulker-resource-utils/src"
	"k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type MinecraftServerResourceBuilder struct {
	Instance *shulkermciov1alpha1.MinecraftServer
	Scheme   *runtime.Scheme
	Client   client.Client
	Ctx      context.Context
}

func (b *MinecraftServerResourceBuilder) ResourceBuilders() ([]common.ResourceBuilder, []common.ResourceBuilder) {
	builders := []common.ResourceBuilder{
		b.MinecraftServerGameServer(),
		b.MinecraftServerConfigMap(),
	}
	dirtyBuilders := []common.ResourceBuilder{}

	if b.Instance.Spec.Configuration.ExistingConfigMapName == "" {
		builders = append(builders, b.MinecraftServerConfigMap())
	}

	return builders, dirtyBuilders
}

func (b *MinecraftServerResourceBuilder) GetGameServerName() string {
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
			labels["minecraftserverfleet.shulkermc.io/name"] = ownerReference.Name
			break
		}
	}

	return labels
}

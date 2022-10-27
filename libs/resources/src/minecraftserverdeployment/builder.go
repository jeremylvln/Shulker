/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
	common "github.com/iamblueslime/shulker/libs/resources/src"
)

type MinecraftServerDeploymentResourceBuilder struct {
	Instance *shulkermciov1alpha1.MinecraftServerDeployment
	Scheme   *runtime.Scheme
}

func (b *MinecraftServerDeploymentResourceBuilder) ResourceBuilders() ([]common.ResourceBuilder, []common.ResourceBuilder) {
	builders := []common.ResourceBuilder{
		b.MinecraftServerDeploymentConfigMap(),
	}
	dirtyBuilders := []common.ResourceBuilder{}

	return builders, dirtyBuilders
}

func (b *MinecraftServerDeploymentResourceBuilder) GetMinecraftServerName(resourceId string) string {
	return fmt.Sprintf("%s-%s", b.Instance.Name, resourceId)
}

func (b *MinecraftServerDeploymentResourceBuilder) GetConfigMapName() string {
	return fmt.Sprintf("%s-config", b.Instance.Name)
}

func (b *MinecraftServerDeploymentResourceBuilder) GetPodSelector() *metav1.LabelSelector {
	return &metav1.LabelSelector{
		MatchLabels: b.getLabels(),
	}
}

func (b *MinecraftServerDeploymentResourceBuilder) getLabels() map[string]string {
	labels := map[string]string{
		"app.kubernetes.io/name":                      b.Instance.Name,
		"app.kubernetes.io/component":                 "proxy",
		"minecraftcluster.shulkermc.io/name":          b.Instance.Spec.ClusterRef.Name,
		"minecraftserverdeployment.shulkermc.io/name": b.Instance.Name,
	}

	return labels
}

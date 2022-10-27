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

type ProxyResourceBuilder struct {
	Instance *shulkermciov1alpha1.Proxy
	Scheme   *runtime.Scheme
}

func (b *ProxyResourceBuilder) ResourceBuilders() ([]common.ResourceBuilder, []common.ResourceBuilder) {
	builders := []common.ResourceBuilder{
		b.ProxyPod(),
	}
	dirtyBuilders := []common.ResourceBuilder{}

	if b.Instance.Spec.Configuration.ExistingConfigMapName == "" {
		builders = append(builders, b.ProxyConfigMap())
	}

	return builders, dirtyBuilders
}

func (b *ProxyResourceBuilder) GetPodName() string {
	return b.Instance.Name
}

func (b *ProxyResourceBuilder) GetConfigMapName() string {
	if b.Instance.Spec.Configuration.ExistingConfigMapName != "" {
		return b.Instance.Spec.Configuration.ExistingConfigMapName
	}
	return fmt.Sprintf("%s-config", b.Instance.Name)
}

func (b *ProxyResourceBuilder) getServiceAccountName() string {
	return fmt.Sprintf("%s-proxy", b.Instance.Spec.ClusterRef.Name)
}

func (b *ProxyResourceBuilder) getLabels() map[string]string {
	labels := map[string]string{
		"app.kubernetes.io/name":             b.Instance.Name,
		"app.kubernetes.io/component":        "proxy",
		"minecraftcluster.shulkermc.io/name": b.Instance.Spec.ClusterRef.Name,
	}

	for _, ownerReference := range b.Instance.OwnerReferences {
		if *ownerReference.Controller {
			labels["app.kubernetes.io/name"] = ownerReference.Name
			labels["app.kubernetes.io/instance"] = b.Instance.Name
			labels["proxydeployment.shulkermc.io/name"] = ownerReference.Name
			break
		}
	}

	return labels
}

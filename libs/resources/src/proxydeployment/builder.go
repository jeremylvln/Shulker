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

type ProxyDeploymentResourceBuilder struct {
	Instance *shulkermciov1alpha1.ProxyDeployment
	Scheme   *runtime.Scheme
}

func (b *ProxyDeploymentResourceBuilder) ResourceBuilders() ([]common.ResourceBuilder, []common.ResourceBuilder) {
	builders := []common.ResourceBuilder{
		b.ProxyDeploymentConfigMap(),
		b.ProxyDeploymentService(),
	}
	dirtyBuilders := []common.ResourceBuilder{}

	return builders, dirtyBuilders
}

func (b *ProxyDeploymentResourceBuilder) GetProxyName(resourceId string) string {
	return fmt.Sprintf("%s-%s", b.Instance.Name, resourceId)
}

func (b *ProxyDeploymentResourceBuilder) GetConfigMapName() string {
	return fmt.Sprintf("%s-config", b.Instance.Name)
}

func (b *ProxyDeploymentResourceBuilder) getServiceName() string {
	return b.Instance.Name
}

func (b *ProxyDeploymentResourceBuilder) GetPodSelector() *metav1.LabelSelector {
	return &metav1.LabelSelector{
		MatchLabels: b.getLabels(),
	}
}

func (b *ProxyDeploymentResourceBuilder) getLabels() map[string]string {
	labels := map[string]string{
		"app.kubernetes.io/name":             b.Instance.Name,
		"app.kubernetes.io/component":        "proxy",
		"minecraftcluster.shulkermc.io/name": b.Instance.Spec.ClusterRef.Name,
		"proxydeployment.shulkermc.io/name":  b.Instance.Name,
	}

	return labels
}

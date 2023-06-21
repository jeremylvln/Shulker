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

type ProxyFleetResourceBuilder struct {
	Instance *shulkermciov1alpha1.ProxyFleet
	Scheme   *runtime.Scheme
	Client   client.Client
	Ctx      context.Context
}

func (b *ProxyFleetResourceBuilder) ResourceBuilders() ([]common.ResourceBuilder, []common.ResourceBuilder) {
	builders := []common.ResourceBuilder{
		b.ProxyFleetFleet(),
		b.ProxyFleetConfigMap(),
		b.ProxyFleetService(),
	}
	dirtyBuilders := []common.ResourceBuilder{}

	if b.Instance.Spec.Template.Spec.Configuration.ExistingConfigMapName == "" {
		builders = append(builders, b.ProxyFleetConfigMap())
	} else {
		builders = append(builders, b.ProxyFleetConfigMap())
	}

	if b.Instance.Spec.Autoscaling != nil {
		if b.Instance.Spec.Autoscaling.AgonesPolicy != nil {
			builders = append(builders, b.ProxyFleetFleetAutoscaler())
		} else {
			dirtyBuilders = append(dirtyBuilders, b.ProxyFleetFleetAutoscaler())
		}
	}

	return builders, dirtyBuilders
}

func (b *ProxyFleetResourceBuilder) GetFleetName() string {
	return b.Instance.Name
}

func (b *ProxyFleetResourceBuilder) GetFleetAutoscalerName() string {
	return b.Instance.Name
}

func (b *ProxyFleetResourceBuilder) GetConfigMapName() string {
	if b.Instance.Spec.Template.Spec.Configuration.ExistingConfigMapName != "" {
		return b.Instance.Spec.Template.Spec.Configuration.ExistingConfigMapName
	}
	return fmt.Sprintf("%s-config", b.Instance.Name)
}

func (b *ProxyFleetResourceBuilder) GetServiceName() string {
	return b.Instance.Name
}

func (b *ProxyFleetResourceBuilder) getServiceAccountName() string {
	return fmt.Sprintf("%s-proxy", b.Instance.Spec.ClusterRef.Name)
}

func (b *ProxyFleetResourceBuilder) getLabels() map[string]string {
	labels := map[string]string{
		"app.kubernetes.io/name":             b.Instance.Name,
		"app.kubernetes.io/component":        "proxy",
		"minecraftcluster.shulkermc.io/name": b.Instance.Spec.ClusterRef.Name,
	}

	for _, ownerReference := range b.Instance.OwnerReferences {
		if *ownerReference.Controller {
			labels["app.kubernetes.io/name"] = ownerReference.Name
			labels["app.kubernetes.io/instance"] = b.Instance.Name
			labels["proxyfleet.shulkermc.io/name"] = ownerReference.Name
			break
		}
	}

	return labels
}

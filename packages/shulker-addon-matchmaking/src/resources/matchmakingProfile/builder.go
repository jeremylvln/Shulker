/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"context"

	matchmakingshulkermciov1alpha1 "github.com/jeremylvln/shulker/packages/shulker-crds/matchmaking/v1alpha1"
	common "github.com/jeremylvln/shulker/packages/shulker-resource-utils/src"
	"k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type MatchmakingProfileResourceBuilder struct {
	Instance *matchmakingshulkermciov1alpha1.MatchmakingProfile
	Scheme   *runtime.Scheme
	Client   client.Client
	Ctx      context.Context
}

func (b *MatchmakingProfileResourceBuilder) ResourceBuilders() ([]common.ResourceBuilder, []common.ResourceBuilder) {
	builders := []common.ResourceBuilder{}
	dirtyBuilders := []common.ResourceBuilder{}

	return builders, dirtyBuilders
}

func (b *MatchmakingProfileResourceBuilder) getLabels() map[string]string {
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

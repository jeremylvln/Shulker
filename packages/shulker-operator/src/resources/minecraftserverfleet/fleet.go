/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"
	"strings"

	agonesapis "agones.dev/agones/pkg/apis"
	agonesv1 "agones.dev/agones/pkg/apis/agones/v1"
	appsv1 "k8s.io/api/apps/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	shulkermciov1alpha1 "github.com/jeremylvln/shulker/packages/shulker-crds/v1alpha1"
	serverresources "github.com/jeremylvln/shulker/packages/shulker-operator/src/resources/minecraftserver"
)

type MinecraftServerFleetResourceFleetBuilder struct {
	*MinecraftServerFleetResourceBuilder
}

func (b *MinecraftServerFleetResourceBuilder) MinecraftServerFleetFleet() *MinecraftServerFleetResourceFleetBuilder {
	return &MinecraftServerFleetResourceFleetBuilder{b}
}

func (b *MinecraftServerFleetResourceFleetBuilder) Build() (client.Object, error) {
	return &agonesv1.Fleet{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.GetFleetName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftServerFleetResourceFleetBuilder) Update(object client.Object) error {
	fleet := object.(*agonesv1.Fleet)

	minecraftServer := shulkermciov1alpha1.MinecraftServer{
		ObjectMeta: metav1.ObjectMeta{
			Namespace: b.Instance.Namespace,
		},
		MinecraftServerTemplate: b.Instance.Spec.Template,
	}
	minecraftServer.Spec.ClusterRef = b.Instance.Spec.ClusterRef
	minecraftServer.Spec.Configuration.ExistingConfigMapName = b.GetConfigMapName()

	minecraftServerResourceBuilder := serverresources.MinecraftServerResourceBuilder{
		Instance: &minecraftServer,
		Scheme:   b.Scheme,
		Client:   b.Client,
		Ctx:      b.Ctx,
	}

	gameServerSpec, err := minecraftServerResourceBuilder.MinecraftServerGameServer().GetGameServerSpec()
	if err != nil {
		return err
	}

	fleet.Spec = agonesv1.FleetSpec{
		Replicas: int32(b.Instance.Spec.Replicas),
		Strategy: appsv1.DeploymentStrategy{
			Type: appsv1.RollingUpdateDeploymentStrategyType,
			RollingUpdate: &appsv1.RollingUpdateDeployment{
				MaxUnavailable: &intstr.IntOrString{
					Type:   intstr.String,
					StrVal: "25%",
				},
				MaxSurge: &intstr.IntOrString{
					Type:   intstr.String,
					StrVal: "25%",
				},
			},
		},
		Scheduling: agonesapis.Packed,
		Template: agonesv1.GameServerTemplateSpec{
			ObjectMeta: metav1.ObjectMeta{
				Labels: b.getLabels(),
				Annotations: map[string]string{
					"minecraftserver.shulkermc.io/tags": strings.Join(b.Instance.Spec.Template.Spec.Tags, ","),
				},
			},
			Spec: *gameServerSpec,
		},
	}

	if err := controllerutil.SetControllerReference(b.Instance, fleet, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Fleet: %v", err)
	}

	return nil
}

func (b *MinecraftServerFleetResourceFleetBuilder) CanBeUpdated() bool {
	return true
}

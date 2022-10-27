/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	minecraftserver "github.com/iamblueslime/shulker/libs/resources/src/minecraftserver"
)

type MinecraftServerDeploymentResourceConfigMapBuilder struct {
	*MinecraftServerDeploymentResourceBuilder
}

func (b *MinecraftServerDeploymentResourceBuilder) MinecraftServerDeploymentConfigMap() *MinecraftServerDeploymentResourceConfigMapBuilder {
	return &MinecraftServerDeploymentResourceConfigMapBuilder{b}
}

func (b *MinecraftServerDeploymentResourceConfigMapBuilder) Build() (client.Object, error) {
	return &corev1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.GetConfigMapName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftServerDeploymentResourceConfigMapBuilder) Update(object client.Object) error {
	configMap := object.(*corev1.ConfigMap)

	configMapData, err := minecraftserver.GetConfigMapDataFromConfigSpec(&b.Instance.Spec.Template.Spec.Configuration)
	if err != nil {
		return err
	}
	configMap.Data = configMapData

	if err := controllerutil.SetControllerReference(b.Instance, configMap, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for ConfigMap: %v", err)
	}

	return nil
}

func (b *MinecraftServerDeploymentResourceConfigMapBuilder) CanBeUpdated() bool {
	return true
}

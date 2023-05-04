/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"

	agonesv1 "agones.dev/agones/pkg/apis/agones/v1"
	rbacv1 "k8s.io/api/rbac/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type MinecraftClusterMinecraftServerRoleBuilder struct {
	*MinecraftClusterResourceBuilder
}

func (b *MinecraftClusterResourceBuilder) MinecraftClusterMinecraftServerRole() *MinecraftClusterMinecraftServerRoleBuilder {
	return &MinecraftClusterMinecraftServerRoleBuilder{b}
}

func (b *MinecraftClusterMinecraftServerRoleBuilder) Build() (client.Object, error) {
	return &rbacv1.Role{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getMinecraftServerRoleName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftClusterMinecraftServerRoleBuilder) Update(object client.Object) error {
	role := object.(*rbacv1.Role)

	role.Rules = []rbacv1.PolicyRule{
		{
			APIGroups: []string{agonesv1.SchemeGroupVersion.Group},
			Resources: []string{"gameservers"},
			Verbs:     []string{"list", "watch", "update"},
		},
		{
			APIGroups: []string{""},
			Resources: []string{"events"},
			Verbs:     []string{"create"},
		},
	}

	if err := controllerutil.SetControllerReference(b.Instance, role, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Role: %v", err)
	}

	return nil
}

func (b *MinecraftClusterMinecraftServerRoleBuilder) CanBeUpdated() bool {
	return true
}

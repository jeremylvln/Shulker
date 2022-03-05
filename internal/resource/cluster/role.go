package resource

import (
	"fmt"

	rbacv1 "k8s.io/api/rbac/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type MinecraftClusterRoleBuilder struct {
	*MinecraftClusterResourceBuilder
}

func (b *MinecraftClusterResourceBuilder) MinecraftClusterRole() *MinecraftClusterRoleBuilder {
	return &MinecraftClusterRoleBuilder{b}
}

func (b *MinecraftClusterRoleBuilder) Build() (client.Object, error) {
	return &rbacv1.Role{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getRoleName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftClusterRoleBuilder) Update(object client.Object) error {
	role := object.(*rbacv1.Role)

	role.Rules = []rbacv1.PolicyRule{
		{
			APIGroups:     []string{"shulkermc.io"},
			Resources:     []string{"minecraftclusters/status"},
			Verbs:         []string{"get", "watch"},
			ResourceNames: []string{b.Instance.Name},
		},
	}

	if err := controllerutil.SetControllerReference(b.Instance, role, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Role: %v", err)
	}

	return nil
}

func (b *MinecraftClusterRoleBuilder) CanBeUpdated() bool {
	return true
}

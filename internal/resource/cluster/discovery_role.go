package resource

import (
	"fmt"

	rbacv1 "k8s.io/api/rbac/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type MinecraftClusterDiscoveryRoleBuilder struct {
	*MinecraftClusterResourceBuilder
}

func (b *MinecraftClusterResourceBuilder) MinecraftClusterDiscoveryRole() *MinecraftClusterDiscoveryRoleBuilder {
	return &MinecraftClusterDiscoveryRoleBuilder{b}
}

func (b *MinecraftClusterDiscoveryRoleBuilder) Build() (client.Object, error) {
	return &rbacv1.Role{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getDiscoveryRoleName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftClusterDiscoveryRoleBuilder) Update(object client.Object) error {
	role := object.(*rbacv1.Role)

	role.Rules = []rbacv1.PolicyRule{
		{
			APIGroups: []string{""},
			Resources: []string{"services", "endpoints"},
			Verbs:     []string{"list"},
		},
		{
			APIGroups:     []string{""},
			Resources:     []string{"services", "endpoints"},
			Verbs:         []string{"get", "watch"},
			ResourceNames: []string{b.getProxyDiscoveryServiceName(), b.getServerDiscoveryServiceName(), b.getServerLobbyDiscoveryServiceName()},
		},
	}

	if err := controllerutil.SetControllerReference(b.Instance, role, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Role: %v", err)
	}

	return nil
}

func (b *MinecraftClusterDiscoveryRoleBuilder) CanBeUpdated() bool {
	return true
}

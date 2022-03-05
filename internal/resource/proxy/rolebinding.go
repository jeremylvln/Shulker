package resource

import (
	"fmt"

	rbacv1 "k8s.io/api/rbac/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type ProxyDeploymentRoleBindingBuilder struct {
	*ProxyDeploymentResourceBuilder
}

func (b *ProxyDeploymentResourceBuilder) ProxyDeploymentRoleBinding() *ProxyDeploymentRoleBindingBuilder {
	return &ProxyDeploymentRoleBindingBuilder{b}
}

func (b *ProxyDeploymentRoleBindingBuilder) Build() (client.Object, error) {
	return &rbacv1.RoleBinding{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getRoleBindingName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
		RoleRef: rbacv1.RoleRef{
			APIGroup: "rbac.authorization.k8s.io",
			Kind:     "Role",
			Name:     fmt.Sprintf("%s-cluster-watch", b.Cluster.Name),
		},
	}, nil
}

func (b *ProxyDeploymentRoleBindingBuilder) Update(object client.Object) error {
	roleBinding := object.(*rbacv1.RoleBinding)

	roleBinding.Subjects = []rbacv1.Subject{{
		Kind:      "ServiceAccount",
		Name:      b.getServiceAccountName(),
		Namespace: b.Instance.Namespace,
	}}

	if err := controllerutil.SetControllerReference(b.Instance, roleBinding, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for RoleBinding: %v", err)
	}

	return nil
}

func (b *ProxyDeploymentRoleBindingBuilder) CanBeUpdated() bool {
	return true
}

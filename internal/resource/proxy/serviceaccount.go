package resource

import (
	"fmt"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type ProxyDeploymentServiceAccountBuilder struct {
	*ProxyDeploymentResourceBuilder
}

func (b *ProxyDeploymentResourceBuilder) ProxyDeploymentServiceAccount() *ProxyDeploymentServiceAccountBuilder {
	return &ProxyDeploymentServiceAccountBuilder{b}
}

func (b *ProxyDeploymentServiceAccountBuilder) Build() (client.Object, error) {
	return &corev1.ServiceAccount{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getServiceAccountName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *ProxyDeploymentServiceAccountBuilder) Update(object client.Object) error {
	serviceAccount := object.(*corev1.ServiceAccount)

	if err := controllerutil.SetControllerReference(b.Instance, serviceAccount, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for ServiceAccount: %v", err)
	}

	return nil
}

func (b *ProxyDeploymentServiceAccountBuilder) CanBeUpdated() bool {
	return true
}

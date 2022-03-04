package resource

import (
	"fmt"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type ProxyDeploymentServiceBuilder struct {
	*ProxyDeploymentResourceBuilder
}

func (b *ProxyDeploymentResourceBuilder) ProxyDeploymentService() *ProxyDeploymentServiceBuilder {
	return &ProxyDeploymentServiceBuilder{b}
}

func (b *ProxyDeploymentServiceBuilder) Build() (client.Object, error) {
	return &corev1.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:        b.getServiceName(),
			Namespace:   b.Instance.Namespace,
			Annotations: b.Instance.Spec.Service.Annotations,
			Labels:      b.getLabels(),
		},
	}, nil
}

func (b *ProxyDeploymentServiceBuilder) Update(object client.Object) error {
	service := object.(*corev1.Service)

	ports := []corev1.ServicePort{{
		Protocol:   corev1.ProtocolTCP,
		Port:       25565,
		TargetPort: intstr.FromInt(25577),
		Name:       "minecraft",
	}}

	service.Spec = corev1.ServiceSpec{
		Selector: b.getLabels(),
		Type:     b.Instance.Spec.Service.Type,
		Ports:    ports,
	}

	if err := controllerutil.SetControllerReference(b.Instance, service, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Service: %v", err)
	}

	return nil
}

func (b *ProxyDeploymentServiceBuilder) CanBeUpdated() bool {
	return true
}

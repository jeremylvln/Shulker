package resource

import (
	"fmt"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type MinecraftServerServiceBuilder struct {
	*MinecraftServerResourceBuilder
}

func (b *MinecraftServerResourceBuilder) MinecraftServerService() *MinecraftServerServiceBuilder {
	return &MinecraftServerServiceBuilder{b}
}

func (b *MinecraftServerServiceBuilder) Build() (client.Object, error) {
	return &corev1.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:        b.getServiceName(),
			Namespace:   b.Instance.Namespace,
			Annotations: b.Instance.Spec.Service.Annotations,
			Labels:      b.getLabels(),
		},
	}, nil
}

func (b *MinecraftServerServiceBuilder) Update(object client.Object) error {
	service := object.(*corev1.Service)

	ports := []corev1.ServicePort{{
		Protocol:   corev1.ProtocolTCP,
		Port:       25565,
		TargetPort: intstr.FromInt(25565),
		Name:       "minecraft",
	}}

	if b.Instance.Spec.Service.ExposesRconPort {
		ports = append(ports, corev1.ServicePort{
			Protocol:   corev1.ProtocolTCP,
			Port:       25575,
			TargetPort: intstr.FromInt(25575),
			Name:       "rcon",
		})
	}

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

func (b *MinecraftServerServiceBuilder) CanBeUpdated() bool {
	return true
}

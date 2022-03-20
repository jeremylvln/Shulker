package resource

import (
	"fmt"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type MinecraftClusterServerDiscoveryServiceBuilder struct {
	*MinecraftClusterResourceBuilder
}

func (b *MinecraftClusterResourceBuilder) MinecraftClusterServerDiscoveryService() *MinecraftClusterServerDiscoveryServiceBuilder {
	return &MinecraftClusterServerDiscoveryServiceBuilder{b}
}

func (b *MinecraftClusterServerDiscoveryServiceBuilder) Build() (client.Object, error) {
	return &corev1.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getServerDiscoveryServiceName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftClusterServerDiscoveryServiceBuilder) Update(object client.Object) error {
	service := object.(*corev1.Service)

	service.Spec = corev1.ServiceSpec{
		Selector: b.getServerPodSelector().MatchLabels,
		Ports: []corev1.ServicePort{{
			Protocol:   corev1.ProtocolTCP,
			Port:       25565,
			TargetPort: intstr.FromInt(25565),
			Name:       "minecraft",
		}},
		Type:      corev1.ServiceTypeClusterIP,
		ClusterIP: corev1.ClusterIPNone,
	}

	if err := controllerutil.SetControllerReference(b.Instance, service, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Service: %v", err)
	}

	return nil
}

func (b *MinecraftClusterServerDiscoveryServiceBuilder) CanBeUpdated() bool {
	return true
}

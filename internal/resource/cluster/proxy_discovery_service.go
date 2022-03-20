package resource

import (
	"fmt"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type MinecraftClusterProxyDiscoveryServiceBuilder struct {
	*MinecraftClusterResourceBuilder
}

func (b *MinecraftClusterResourceBuilder) MinecraftClusterProxyDiscoveryService() *MinecraftClusterProxyDiscoveryServiceBuilder {
	return &MinecraftClusterProxyDiscoveryServiceBuilder{b}
}

func (b *MinecraftClusterProxyDiscoveryServiceBuilder) Build() (client.Object, error) {
	return &corev1.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getProxyDiscoveryServiceName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftClusterProxyDiscoveryServiceBuilder) Update(object client.Object) error {
	service := object.(*corev1.Service)

	service.Spec = corev1.ServiceSpec{
		Selector: b.getProxyPodSelector().MatchLabels,
		Ports: []corev1.ServicePort{{
			Protocol:   corev1.ProtocolTCP,
			Port:       25565,
			TargetPort: intstr.FromInt(25577),
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

func (b *MinecraftClusterProxyDiscoveryServiceBuilder) CanBeUpdated() bool {
	return true
}

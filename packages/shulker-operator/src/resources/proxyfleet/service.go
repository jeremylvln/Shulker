/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type ProxyFleetServiceBuilder struct {
	*ProxyFleetResourceBuilder
}

func (b *ProxyFleetResourceBuilder) ProxyFleetService() *ProxyFleetServiceBuilder {
	return &ProxyFleetServiceBuilder{b}
}

func (b *ProxyFleetServiceBuilder) Build() (client.Object, error) {
	return &corev1.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.GetServiceName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *ProxyFleetServiceBuilder) Update(object client.Object) error {
	service := object.(*corev1.Service)
	service.ObjectMeta.Annotations = b.Instance.Spec.Service.Annotations

	ports := []corev1.ServicePort{{
		Name:       "minecraft",
		Protocol:   corev1.ProtocolTCP,
		Port:       25565,
		TargetPort: intstr.FromInt(25577),
	}}

	service.Spec = corev1.ServiceSpec{
		Selector:              b.getLabels(),
		Type:                  b.Instance.Spec.Service.Type,
		ExternalTrafficPolicy: b.Instance.Spec.Service.ExternalTrafficPolicy,
		Ports:                 ports,
	}

	if err := controllerutil.SetControllerReference(b.Instance, service, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Service: %v", err)
	}

	return nil
}

func (b *ProxyFleetServiceBuilder) CanBeUpdated() bool {
	return true
}

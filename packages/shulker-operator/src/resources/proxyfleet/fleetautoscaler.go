/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"

	agonesautoscalingv1 "agones.dev/agones/pkg/apis/autoscaling/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type ProxyFleetResourceFleetAutoscalerBuilder struct {
	*ProxyFleetResourceBuilder
}

func (b *ProxyFleetResourceBuilder) ProxyFleetFleetAutoscaler() *ProxyFleetResourceFleetAutoscalerBuilder {
	return &ProxyFleetResourceFleetAutoscalerBuilder{b}
}

func (b *ProxyFleetResourceFleetAutoscalerBuilder) Build() (client.Object, error) {
	return &agonesautoscalingv1.FleetAutoscaler{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.GetFleetAutoscalerName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *ProxyFleetResourceFleetAutoscalerBuilder) Update(object client.Object) error {
	fleetAutoscaler := object.(*agonesautoscalingv1.FleetAutoscaler)

	fleetAutoscaler.Spec = agonesautoscalingv1.FleetAutoscalerSpec{
		FleetName: b.GetFleetName(),
		Policy:    *b.Instance.Spec.Autoscaling.AgonesPolicy,
	}

	if err := controllerutil.SetControllerReference(b.Instance, fleetAutoscaler, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for FleetAutoscaler: %v", err)
	}

	return nil
}

func (b *ProxyFleetResourceFleetAutoscalerBuilder) CanBeUpdated() bool {
	return true
}

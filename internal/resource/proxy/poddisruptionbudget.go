package resource

import (
	"fmt"

	policyv1 "k8s.io/api/policy/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type ProxyDeploymentPodDisruptionBudgetBuilder struct {
	*ProxyDeploymentResourceBuilder
}

func (b *ProxyDeploymentResourceBuilder) ProxyDeploymentPodDisruptionBudget() *ProxyDeploymentPodDisruptionBudgetBuilder {
	return &ProxyDeploymentPodDisruptionBudgetBuilder{b}
}

func (b *ProxyDeploymentPodDisruptionBudgetBuilder) Build() (client.Object, error) {
	return &policyv1.PodDisruptionBudget{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getPodDisruptionBudgetName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *ProxyDeploymentPodDisruptionBudgetBuilder) Update(object client.Object) error {
	podDisruptionBudget := object.(*policyv1.PodDisruptionBudget)
	podDisruptionBudget.Spec.Selector = b.GetPodSelector()

	if b.Instance.Spec.DisruptionBudget.MinAvailable != nil {
		value := intstr.FromInt(int(*b.Instance.Spec.DisruptionBudget.MinAvailable))
		podDisruptionBudget.Spec.MinAvailable = &value
	} else {
		value := intstr.FromInt(int(*b.Instance.Spec.DisruptionBudget.MaxUnavailable))
		podDisruptionBudget.Spec.MaxUnavailable = &value
	}

	if err := controllerutil.SetControllerReference(b.Instance, podDisruptionBudget, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for PodDisruptionBudget: %v", err)
	}

	return nil
}

func (b *ProxyDeploymentPodDisruptionBudgetBuilder) CanBeUpdated() bool {
	return true
}

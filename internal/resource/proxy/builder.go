package resource

import (
	"fmt"

	"k8s.io/apimachinery/pkg/runtime"
	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
	common "shulkermc.io/m/v2/internal/resource"
)

type ProxyDeploymentResourceBuilder struct {
	Instance *shulkermciov1alpha1.ProxyDeployment
	Scheme   *runtime.Scheme
}

func (b *ProxyDeploymentResourceBuilder) ResourceBuilders() ([]common.ResourceBuilder, []common.ResourceBuilder) {
	builders := []common.ResourceBuilder{
		b.ProxyDeploymentDeployment(),
		b.ProxyDeploymentService(),
	}
	dirtyBuilders := []common.ResourceBuilder{}

	return builders, dirtyBuilders
}

func (b *ProxyDeploymentResourceBuilder) getResourcePrefix() string {
	if b.Instance.Spec.ClusterRef != nil {
		return b.Instance.Spec.ClusterRef.Name
	} else {
		return "minecraft"
	}
}

func (b *ProxyDeploymentResourceBuilder) GetDeploymentName() string {
	return fmt.Sprintf("%s-proxy-%s", b.getResourcePrefix(), b.Instance.Name)
}

func (b *ProxyDeploymentResourceBuilder) getServiceName() string {
	return fmt.Sprintf("%s-proxy-%s", b.getResourcePrefix(), b.Instance.Name)
}

func (b *ProxyDeploymentResourceBuilder) getLabels() map[string]string {
	labels := map[string]string{
		"app.kubernetes.io/name":          b.Instance.Name,
		"app.kubernetes.io/component":     "proxy",
		"app.kubernetes.io/part-of":       "shulker",
		"app.kubernetes.io/created-by":    "shulker",
		"proxydeployment.shulker.io/name": b.Instance.Name,
	}

	if b.Instance.Spec.ClusterRef != nil {
		labels["proxydeployment.shulker.io/cluster-name"] = b.Instance.Spec.ClusterRef.Name
	}

	return labels
}

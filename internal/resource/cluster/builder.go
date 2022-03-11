package resource

import (
	"fmt"

	"k8s.io/apimachinery/pkg/runtime"
	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
	common "shulkermc.io/m/v2/internal/resource"
)

type MinecraftClusterResourceBuilder struct {
	Instance *shulkermciov1alpha1.MinecraftCluster
	Scheme   *runtime.Scheme
}

func (b *MinecraftClusterResourceBuilder) ResourceBuilders() ([]common.ResourceBuilder, []common.ResourceBuilder) {
	builders := []common.ResourceBuilder{
		b.MinecraftClusterRole(),
	}
	dirtyBuilders := []common.ResourceBuilder{}

	return builders, dirtyBuilders
}

func (b *MinecraftClusterResourceBuilder) getRoleName() string {
	return fmt.Sprintf("%s-cluster-watch-pool", b.Instance.Name)
}

func (b *MinecraftClusterResourceBuilder) getLabels() map[string]string {
	return map[string]string{
		"app.kubernetes.io/name":       b.Instance.Name,
		"app.kubernetes.io/component":  "minecraft-cluster",
		"app.kubernetes.io/part-of":    "shulker",
		"app.kubernetes.io/created-by": "shulker",
	}
}

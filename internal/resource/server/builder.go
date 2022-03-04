package resource

import (
	"fmt"

	"k8s.io/apimachinery/pkg/runtime"
	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
	common "shulkermc.io/m/v2/internal/resource"
)

type MinecraftServerResourceBuilder struct {
	Instance *shulkermciov1alpha1.MinecraftServer
	Scheme   *runtime.Scheme
}

func (b *MinecraftServerResourceBuilder) ResourceBuilders() ([]common.ResourceBuilder, []common.ResourceBuilder) {
	builders := []common.ResourceBuilder{
		b.MinecraftServerPod(),
		b.MinecraftServerConfigMap(),
	}
	dirtyBuilders := []common.ResourceBuilder{}

	if b.Instance.Spec.Rcon.Enabled {
		builders = append(builders, b.MinecraftServerRconSecret())
	} else {
		dirtyBuilders = append(dirtyBuilders, b.MinecraftServerRconSecret())
	}

	if b.Instance.Spec.Service.Enabled {
		builders = append(builders, b.MinecraftServerService())
	} else {
		dirtyBuilders = append(dirtyBuilders, b.MinecraftServerService())
	}

	return builders, dirtyBuilders
}

func (b *MinecraftServerResourceBuilder) getResourcePrefix() string {
	if b.Instance.Spec.ClusterRef != nil {
		return b.Instance.Spec.ClusterRef.Name
	} else {
		return "minecraft"
	}
}

func (b *MinecraftServerResourceBuilder) GetPodName() string {
	return fmt.Sprintf("%s-server-%s", b.getResourcePrefix(), b.Instance.Name)
}

func (b *MinecraftServerResourceBuilder) getConfigMapName() string {
	return fmt.Sprintf("%s-server-config-%s", b.getResourcePrefix(), b.Instance.Name)
}

func (b *MinecraftServerResourceBuilder) getRconSecretName() string {
	return fmt.Sprintf("%s-server-rcon-secret-%s", b.getResourcePrefix(), b.Instance.Name)
}

func (b *MinecraftServerResourceBuilder) getServiceName() string {
	return fmt.Sprintf("%s-server-%s", b.getResourcePrefix(), b.Instance.Name)
}

func (b *MinecraftServerResourceBuilder) getLabels() map[string]string {
	labels := map[string]string{
		"app.kubernetes.io/name":          b.Instance.Name,
		"app.kubernetes.io/component":     "minecraft-server",
		"app.kubernetes.io/part-of":       "shulker",
		"app.kubernetes.io/created-by":    "shulker",
		"minecraftserver.shulker.io/name": b.Instance.Name,
	}

	if b.Instance.Spec.ClusterRef != nil {
		labels["minecraftserver.shulker.io/cluster-name"] = b.Instance.Spec.ClusterRef.Name
	}

	return labels
}

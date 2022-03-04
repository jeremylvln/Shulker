package resource

import (
	"fmt"

	"k8s.io/apimachinery/pkg/runtime"
	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type MinecraftServerResourceBuilder struct {
	Instance *shulkermciov1alpha1.MinecraftServer
	Scheme   *runtime.Scheme
}

type ResourceBuilder interface {
	Build() (client.Object, error)
	Update(client.Object) error
	CanBeUpdated() bool
}

func (b *MinecraftServerResourceBuilder) ResourceBuilders() ([]ResourceBuilder, []ResourceBuilder) {
	builders := []ResourceBuilder{
		b.MinecraftServerPod(),
		b.MinecraftServerConfigMap(),
	}
	dirtyBuilders := []ResourceBuilder{}

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

func (b *MinecraftServerResourceBuilder) getPodName() string {
	return fmt.Sprintf("minecraft-server-%s", b.Instance.Name)
}

func (b *MinecraftServerResourceBuilder) getConfigMapName() string {
	return fmt.Sprintf("minecraft-server-config-%s", b.Instance.Name)
}

func (b *MinecraftServerResourceBuilder) getRconSecretName() string {
	return fmt.Sprintf("minecraft-server-rcon-secret-%s", b.Instance.Name)
}

func (b *MinecraftServerResourceBuilder) getServiceName() string {
	return fmt.Sprintf("minecraft-server-%s", b.Instance.Name)
}

func (b *MinecraftServerResourceBuilder) getLabels() map[string]string {
	return map[string]string{
		"app.kubernetes.io/name":          b.Instance.Name,
		"app.kubernetes.io/component":     "minecraft-server",
		"app.kubernetes.io/part-of":       "shulker",
		"app.kubernetes.io/created-by":    "shulker",
		"minecraftserver.shulker.io/name": b.Instance.Name,
	}
}
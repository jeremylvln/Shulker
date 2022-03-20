package resource

import (
	"fmt"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
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
		b.MinecraftClusterProxyDiscoveryService(),
		b.MinecraftClusterServerDiscoveryService(),
		b.MinecraftClusterServerLobbyDiscoveryService(),
		b.MinecraftClusterDiscoveryRole(),
	}
	dirtyBuilders := []common.ResourceBuilder{}

	if b.Instance.Spec.LimboSpec.Enabled {
		builders = append(builders, b.MinecraftClusterLimboMinecraftServerDeployment())
	} else {
		dirtyBuilders = append(dirtyBuilders, b.MinecraftClusterLimboMinecraftServerDeployment())
	}

	return builders, dirtyBuilders
}

func (b *MinecraftClusterResourceBuilder) getResourcePrefix() string {
	return b.Instance.Name
}

func (b *MinecraftClusterResourceBuilder) getProxyDiscoveryServiceName() string {
	return fmt.Sprintf("%s-proxy-discovery", b.getResourcePrefix())
}

func (b *MinecraftClusterResourceBuilder) getServerDiscoveryServiceName() string {
	return fmt.Sprintf("%s-server-discovery", b.getResourcePrefix())
}

func (b *MinecraftClusterResourceBuilder) getServerLobbyDiscoveryServiceName() string {
	return fmt.Sprintf("%s-server-lobby-discovery", b.getResourcePrefix())
}

func (b *MinecraftClusterResourceBuilder) getLimboMinecraftServerDeploymentName() string {
	return "limbo"
}

func (b *MinecraftClusterResourceBuilder) getDiscoveryRoleName() string {
	return fmt.Sprintf("%s-discovery", b.Instance.Name)
}

func (b *MinecraftClusterResourceBuilder) getProxyPodSelector() *metav1.LabelSelector {
	return &metav1.LabelSelector{
		MatchLabels: map[string]string{
			"app.kubernetes.io/component":        "proxy",
			"minecraftcluster.shulkermc.io/name": b.Instance.Name,
		},
	}
}

func (b *MinecraftClusterResourceBuilder) getServerPodSelector() *metav1.LabelSelector {
	return &metav1.LabelSelector{
		MatchLabels: map[string]string{
			"app.kubernetes.io/component":        "minecraft-server",
			"minecraftcluster.shulkermc.io/name": b.Instance.Name,
		},
	}
}

func (b *MinecraftClusterResourceBuilder) getServerLobbyPodSelector() *metav1.LabelSelector {
	return &metav1.LabelSelector{
		MatchLabels: map[string]string{
			"app.kubernetes.io/component":                      "minecraft-server",
			"minecraftcluster.shulkermc.io/name":               b.Instance.Name,
			"minecraftserverdeployment.shulkermc.io/tag-lobby": "true",
		},
	}
}

func (b *MinecraftClusterResourceBuilder) getLabels() map[string]string {
	return map[string]string{
		"app.kubernetes.io/name":       b.Instance.Name,
		"app.kubernetes.io/component":  "minecraft-cluster",
		"app.kubernetes.io/part-of":    b.Instance.Name,
		"app.kubernetes.io/created-by": "shulker-operator",
	}
}

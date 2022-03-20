package resource

import (
	"fmt"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	"k8s.io/apimachinery/pkg/runtime"
	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
	common "shulkermc.io/m/v2/internal/resource"
)

type MinecraftServerDeploymentResourceBuilder struct {
	Instance *shulkermciov1alpha1.MinecraftServerDeployment
	Cluster  *shulkermciov1alpha1.MinecraftCluster
	Scheme   *runtime.Scheme
}

func (b *MinecraftServerDeploymentResourceBuilder) ResourceBuilders() ([]common.ResourceBuilder, []common.ResourceBuilder) {
	builders := []common.ResourceBuilder{
		b.MinecraftServerDeploymentDeployment(),
		b.MinecraftServerDeploymentConfigMap(),
	}
	dirtyBuilders := []common.ResourceBuilder{}

	if b.Instance.Spec.Rcon.Enabled {
		builders = append(builders, b.MinecraftServerRconSecret())
	} else {
		dirtyBuilders = append(dirtyBuilders, b.MinecraftServerRconSecret())
	}

	return builders, dirtyBuilders
}

func (b *MinecraftServerDeploymentResourceBuilder) getResourcePrefix() string {
	return b.Instance.Spec.ClusterRef.Name
}

func (b *MinecraftServerDeploymentResourceBuilder) GetDeploymentName() string {
	return fmt.Sprintf("%s-server-%s", b.getResourcePrefix(), b.Instance.Name)
}

func (b *MinecraftServerDeploymentResourceBuilder) getConfigMapName() string {
	return fmt.Sprintf("%s-server-config-%s", b.getResourcePrefix(), b.Instance.Name)
}

func (b *MinecraftServerDeploymentResourceBuilder) getRconSecretName() string {
	return fmt.Sprintf("%s-server-rcon-secret-%s", b.getResourcePrefix(), b.Instance.Name)
}

func (b *MinecraftServerDeploymentResourceBuilder) GetPodSelector() *metav1.LabelSelector {
	return &metav1.LabelSelector{
		MatchLabels: b.getLabels(),
	}
}

func (b *MinecraftServerDeploymentResourceBuilder) getLabels() map[string]string {
	return map[string]string{
		"app.kubernetes.io/name":                      b.Instance.Name,
		"app.kubernetes.io/component":                 "minecraft-server",
		"app.kubernetes.io/part-of":                   b.Instance.Spec.ClusterRef.Name,
		"app.kubernetes.io/created-by":                "shulker-operator",
		"minecraftcluster.shulkermc.io/name":          b.Instance.Spec.ClusterRef.Name,
		"minecraftserverdeployment.shulkermc.io/name": b.Instance.Name,
	}
}

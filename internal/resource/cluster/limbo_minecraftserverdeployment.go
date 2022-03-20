package resource

import (
	"fmt"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type MinecraftClusterLimboMinecraftServerDeploymentBuilder struct {
	*MinecraftClusterResourceBuilder
}

func (b *MinecraftClusterResourceBuilder) MinecraftClusterLimboMinecraftServerDeployment() *MinecraftClusterLimboMinecraftServerDeploymentBuilder {
	return &MinecraftClusterLimboMinecraftServerDeploymentBuilder{b}
}

func (b *MinecraftClusterLimboMinecraftServerDeploymentBuilder) Build() (client.Object, error) {
	return &shulkermciov1alpha1.MinecraftServerDeployment{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getLimboMinecraftServerDeploymentName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftClusterLimboMinecraftServerDeploymentBuilder) Update(object client.Object) error {
	minecraftServerDeployment := object.(*shulkermciov1alpha1.MinecraftServerDeployment)
	maxPlayers := int16(-1)

	minecraftServerDeployment.Spec = shulkermciov1alpha1.MinecraftServerDeploymentSpec{
		ClusterRef: shulkermciov1alpha1.MinecraftClusterRef{
			Name: b.Instance.Name,
		},
		Replicas: 1,
		Tags:     []string{"limbo"},
		Version: shulkermciov1alpha1.MinecraftServerDeploymentVersionSpec{
			Channel: shulkermciov1alpha1.MinecraftServerDeploymentVersionLimbo,
			Name:    "latest",
		},
		MaxPlayers: &maxPlayers,
		World: &shulkermciov1alpha1.MinecraftServerDeploymentWorldSpec{
			SchematicUrl:        b.Instance.Spec.LimboSpec.SchematicUrl,
			SchematicWorldSpawn: b.Instance.Spec.LimboSpec.SpawnPosition,
		},
		PodOverrides: &shulkermciov1alpha1.MinecraftServerDeploymentPodOverridesSpec{
			LivenessProbe: &shulkermciov1alpha1.MinecraftServerDeploymentPodProbeSpec{
				InitialDelaySeconds: 10,
			},
			ReadinessProbe: &shulkermciov1alpha1.MinecraftServerDeploymentPodProbeSpec{
				InitialDelaySeconds: 10,
			},
		},
		Resources: b.Instance.Spec.LimboSpec.Resources,
		Affinity:  b.Instance.Spec.LimboSpec.Affinity,
	}

	if err := controllerutil.SetControllerReference(b.Instance, minecraftServerDeployment, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for MinecraftServerDeployment: %v", err)
	}

	return nil
}

func (b *MinecraftClusterLimboMinecraftServerDeploymentBuilder) CanBeUpdated() bool {
	return true
}

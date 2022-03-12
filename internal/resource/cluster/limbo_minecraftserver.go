package resource

import (
	"fmt"

	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

type MinecraftClusterLimboMinecraftServerBuilder struct {
	*MinecraftClusterResourceBuilder
}

func (b *MinecraftClusterResourceBuilder) MinecraftClusterLimboMinecraftServer() *MinecraftClusterLimboMinecraftServerBuilder {
	return &MinecraftClusterLimboMinecraftServerBuilder{b}
}

func (b *MinecraftClusterLimboMinecraftServerBuilder) Build() (client.Object, error) {
	return &shulkermciov1alpha1.MinecraftServer{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getLimboMinecraftServerName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftClusterLimboMinecraftServerBuilder) Update(object client.Object) error {
	minecraftServer := object.(*shulkermciov1alpha1.MinecraftServer)

	maxPlayers := int16(300)

	minecraftServer.Spec = shulkermciov1alpha1.MinecraftServerSpec{
		ClusterRef: &shulkermciov1alpha1.MinecraftClusterRef{
			Name: b.Instance.Name,
		},
		Tags: []string{"limbo"},
		Version: shulkermciov1alpha1.MinecraftServerVersionSpec{
			Channel: shulkermciov1alpha1.MinecraftServerVersionLimbo,
			Name:    "latest",
		},
		MaxPlayers: &maxPlayers,
		World: &shulkermciov1alpha1.MinecraftServerWorldSpec{
			SchematicUrl: "https://i.jeremylvln.fr/shulker/limbo.schematic",
		},
		PodOverrides: &shulkermciov1alpha1.MinecraftServerPodOverridesSpec{
			LivenessProbe: &shulkermciov1alpha1.MinecraftServerPodProbeSpec{
				InitialDelaySeconds: 10,
			},
			ReadinessProbe: &shulkermciov1alpha1.MinecraftServerPodProbeSpec{
				InitialDelaySeconds: 10,
			},
		},
		Resources: &corev1.ResourceRequirements{
			Requests: corev1.ResourceList{
				"cpu":    *resource.NewScaledQuantity(250, resource.Milli),
				"memory": *resource.NewScaledQuantity(128, resource.Mega),
			},
			Limits: corev1.ResourceList{
				"cpu":    *resource.NewScaledQuantity(500, resource.Milli),
				"memory": *resource.NewScaledQuantity(512, resource.Mega),
			},
		},
	}

	if err := controllerutil.SetControllerReference(b.Instance, minecraftServer, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for MinecraftServer: %v", err)
	}

	return nil
}

func (b *MinecraftClusterLimboMinecraftServerBuilder) CanBeUpdated() bool {
	return true
}

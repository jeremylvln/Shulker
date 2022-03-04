package resource

import (
	"fmt"
	"strconv"
	"strings"

	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
	k8sresource "k8s.io/apimachinery/pkg/api/resource"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
)

const serverConfigDir = "/config"
const serverDataDir = "/data"

type MinecraftServerPodBuilder struct {
	*MinecraftServerResourceBuilder
}

func (b *MinecraftServerResourceBuilder) MinecraftServerPod() *MinecraftServerPodBuilder {
	return &MinecraftServerPodBuilder{b}
}

func (b *MinecraftServerPodBuilder) Build() (client.Object, error) {
	return &corev1.Pod{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.getPodName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftServerPodBuilder) Update(object client.Object) error {
	pod := object.(*corev1.Pod)

	pod.Spec = corev1.PodSpec{
		InitContainers: []corev1.Container{{
			Image:   "busybox:stable",
			Name:    "init-server-fs",
			Command: []string{"ash", fmt.Sprintf("%s/init-fs.sh", serverConfigDir)},
			Env: []corev1.EnvVar{
				{
					Name:  "SHULKER_CONFIG_DIR",
					Value: serverConfigDir,
				},
				{
					Name:  "SHULKER_DATA_DIR",
					Value: serverDataDir,
				},
			},
			Resources: corev1.ResourceRequirements{
				Limits: corev1.ResourceList{
					"cpu":    k8sresource.MustParse("500m"),
					"memory": k8sresource.MustParse("128Mi"),
				},
				Requests: corev1.ResourceList{
					"cpu":    k8sresource.MustParse("10m"),
					"memory": k8sresource.MustParse("512Ki"),
				},
			},
			VolumeMounts: []corev1.VolumeMount{
				{
					Name:      "minecraft-data-dir",
					MountPath: serverDataDir,
				},
				{
					Name:      "minecraft-config-dir",
					MountPath: serverConfigDir,
					ReadOnly:  true,
				},
			},
		}},
		Containers: []corev1.Container{{
			Image: "itzg/minecraft-server:latest",
			Name:  "minecraft-server",
			Ports: []corev1.ContainerPort{{
				ContainerPort: 25565,
				Name:          "minecraft",
			}, {
				ContainerPort: 25575,
				Name:          "rcon",
			}},
			Env: b.getPodEnv(),
			LivenessProbe: &corev1.Probe{
				ProbeHandler: corev1.ProbeHandler{
					TCPSocket: &corev1.TCPSocketAction{
						Port: intstr.FromInt(25565),
					},
				},
				InitialDelaySeconds: b.Instance.Spec.PodOverrides.LivenessProbe.InitialDelaySeconds,
				PeriodSeconds:       10,
			},
			ReadinessProbe: &corev1.Probe{
				ProbeHandler: corev1.ProbeHandler{
					TCPSocket: &corev1.TCPSocketAction{
						Port: intstr.FromInt(25565),
					},
				},
				InitialDelaySeconds: b.Instance.Spec.PodOverrides.ReadinessProbe.InitialDelaySeconds,
				PeriodSeconds:       10,
			},
			Resources: *b.Instance.Spec.Resources,
			VolumeMounts: []corev1.VolumeMount{
				{
					Name:      "minecraft-data-dir",
					MountPath: serverDataDir,
				},
			},
		}},
		TerminationGracePeriodSeconds: b.Instance.Spec.PodOverrides.TerminationGracePeriodSeconds,
		RestartPolicy:                 corev1.RestartPolicyNever,
		Affinity:                      b.Instance.Spec.Affinity,
		Volumes: []corev1.Volume{
			{
				Name: "minecraft-data-dir",
				VolumeSource: corev1.VolumeSource{
					EmptyDir: &corev1.EmptyDirVolumeSource{},
				},
			},
			{
				Name: "minecraft-config-dir",
				VolumeSource: corev1.VolumeSource{
					ConfigMap: &corev1.ConfigMapVolumeSource{
						LocalObjectReference: corev1.LocalObjectReference{
							Name: b.getConfigMapName(),
						},
					},
				},
			},
		},
	}

	if err := controllerutil.SetControllerReference(b.Instance, pod, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Pod: %v", err)
	}

	return nil
}

func (b *MinecraftServerPodBuilder) CanBeUpdated() bool {
	return false
}

func (b *MinecraftServerPodBuilder) getPodEnv() []corev1.EnvVar {
	shouldEnforceWhitelist := len(b.Instance.Spec.WhitelistedPlayers) > 0
	env := append([]corev1.EnvVar{
		{
			Name:  "SERVER_NAME",
			Value: b.Instance.Name,
		},
		{
			Name:  "TYPE",
			Value: getTypeFromVersionChannel(b.Instance.Spec.Version.Channel),
		},
		{
			Name:  "VERSION",
			Value: b.Instance.Spec.Version.Name,
		},
		{
			Name:  "EULA",
			Value: "true",
		},
		{
			Name:  "SERVER_PORT",
			Value: "25565",
		},
		{
			Name:  "ENABLE_RCON",
			Value: "true",
		},
		{
			Name:  "RCON_PORT",
			Value: "25575",
		},
		{
			Name:  "RCON_PASSWORD",
			Value: "hello", // TODO: change me
		},
		{
			Name:  "MAX_PLAYERS",
			Value: fmt.Sprintf("%d", *b.Instance.Spec.MaxPlayers),
		},
		{
			Name:  "MOTD",
			Value: b.Instance.Spec.Motd,
		},
		{
			Name:  "OPS",
			Value: strings.Join(b.Instance.Spec.Operators, ","),
		},
		{
			Name:  "WHITELIST",
			Value: strings.Join(b.Instance.Spec.WhitelistedPlayers, ","),
		},
		{
			Name:  "ENFORCE_WHITELIST",
			Value: strconv.FormatBool(shouldEnforceWhitelist),
		},
		{
			Name:  "ALLOW_NETHER",
			Value: strconv.FormatBool(!b.Instance.Spec.World.DisableEnd),
		},
		{
			Name:  "INIT_MEMORY",
			Value: fmt.Sprintf("%dM", b.Instance.Spec.Resources.Requests.Memory().ScaledValue(resource.Mega)),
		},
		{
			Name:  "MAX_MEMORY",
			Value: fmt.Sprintf("%dM", b.Instance.Spec.Resources.Limits.Memory().ScaledValue(resource.Mega)-1000),
		},
		{
			Name:  "WORLD",
			Value: "https://i.jeremylvln.fr/shulker/hub.tar.gz",
		},
	}, b.Instance.Spec.PodOverrides.Env...)

	return env
}

func getTypeFromVersionChannel(channel shulkermciov1alpha1.MinecraftServerVersionChannel) string {
	switch channel {
	case shulkermciov1alpha1.MinecraftServerVersionVanilla:
		return "VANILLA"
	case shulkermciov1alpha1.MinecraftServerVersionForge:
		return "FORGE"
	case shulkermciov1alpha1.MinecraftServerVersionFabric:
		return "FABRIC"
	case shulkermciov1alpha1.MinecraftServerVersionSpigot:
		return "SPIGOT"
	case shulkermciov1alpha1.MinecraftServerVersionPaper:
		return "PAPER"
	case shulkermciov1alpha1.MinecraftServerVersionAirplace:
		return "AIRPLANE"
	case shulkermciov1alpha1.MinecraftServerVersionPufferfish:
		return "PUFFERFISH"
	case shulkermciov1alpha1.MinecraftServerVersionPurpur:
		return "PURPUR"
	case shulkermciov1alpha1.MinecraftServerVersionMagma:
		return "MAGMA"
	case shulkermciov1alpha1.MinecraftServerVersionMohist:
		return "MOHIST"
	case shulkermciov1alpha1.MinecraftServerVersionCatserver:
		return "CATSERVER"
	case shulkermciov1alpha1.MinecraftServerVersionCanyon:
		return "CANYON"
	case shulkermciov1alpha1.MinecraftServerVersionSpongeVanilla:
		return "SPONGEVANILLA"
	case shulkermciov1alpha1.MinecraftServerVersionLimbo:
		return "LIMBO"
	case shulkermciov1alpha1.MinecraftServerVersionCrucible:
		return "CRUCIBLE"
	}

	return ""
}

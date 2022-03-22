package resource

import (
	"fmt"
	"strconv"
	"strings"

	appsv1 "k8s.io/api/apps/v1"
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

type MinecraftServerDeploymentDeploymentBuilder struct {
	*MinecraftServerDeploymentResourceBuilder
}

func (b *MinecraftServerDeploymentResourceBuilder) MinecraftServerDeploymentDeployment() *MinecraftServerDeploymentDeploymentBuilder {
	return &MinecraftServerDeploymentDeploymentBuilder{b}
}

func (b *MinecraftServerDeploymentDeploymentBuilder) Build() (client.Object, error) {
	return &appsv1.Deployment{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.GetDeploymentName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftServerDeploymentDeploymentBuilder) Update(object client.Object) error {
	deployment := object.(*appsv1.Deployment)
	revisionHistoryLimit := int32(1)

	deployment.Spec = appsv1.DeploymentSpec{
		Replicas: &b.Instance.Spec.Replicas,
		Selector: b.GetPodSelector(),
		Template: corev1.PodTemplateSpec{
			ObjectMeta: metav1.ObjectMeta{
				Labels: b.getLabels(),
			},
			Spec: corev1.PodSpec{
				InitContainers: b.getInitContainers(),
				Containers: []corev1.Container{{
					Image: "itzg/minecraft-server:latest",
					Name:  "minecraft-server",
					Ports: b.getDeploymentPorts(),
					Env:   b.getDeploymentEnv(),
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
					Resources:       *b.Instance.Spec.Resources,
					SecurityContext: b.getSecurityContext(),
					VolumeMounts: []corev1.VolumeMount{
						{
							Name:      "minecraft-data-dir",
							MountPath: serverDataDir,
						},
					},
				}},
				TerminationGracePeriodSeconds: b.Instance.Spec.PodOverrides.TerminationGracePeriodSeconds,
				RestartPolicy:                 corev1.RestartPolicyAlways,
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
			},
		},
		RevisionHistoryLimit: &revisionHistoryLimit,
	}

	if err := controllerutil.SetControllerReference(b.Instance, deployment, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Deployment: %v", err)
	}

	return nil
}

func (b *MinecraftServerDeploymentDeploymentBuilder) CanBeUpdated() bool {
	return false
}

func (b *MinecraftServerDeploymentDeploymentBuilder) getInitContainers() []corev1.Container {
	containers := []corev1.Container{{
		Image:   "busybox:stable",
		Name:    "init-server-fs",
		Command: []string{"ash", fmt.Sprintf("%s/init-fs.sh", serverConfigDir)},
		Env:     b.getDeploymentInitFsEnv(),
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
		SecurityContext: b.getSecurityContext(),
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
	}}

	if len(b.Instance.Spec.Plugins) > 0 {
		containers = append(containers, corev1.Container{
			Image:   "curlimages/curl:latest",
			Name:    "init-proxy-plugins",
			Command: []string{"ash", fmt.Sprintf("%s/init-plugins.sh", serverConfigDir)},
			Env:     b.getDeploymentInitPluginsEnv(),
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
			SecurityContext: b.getSecurityContext(),
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
		})
	}

	if b.Instance.Spec.World != nil && b.Instance.Spec.World.SchematicUrl != "" {
		containers = append(containers, corev1.Container{
			Image:   "curlimages/curl:latest",
			Name:    "init-limbo-schematic",
			Command: []string{"ash", fmt.Sprintf("%s/init-limbo-schematic.sh", serverConfigDir)},
			Env: []corev1.EnvVar{
				{
					Name:  "SHULKER_CONFIG_DIR",
					Value: serverConfigDir,
				},
				{
					Name:  "SHULKER_DATA_DIR",
					Value: serverDataDir,
				},
				{
					Name:  "SHULKER_LIMBO_SCHEMATIC_URL",
					Value: b.Instance.Spec.World.SchematicUrl,
				},
				{
					Name:  "SHULKER_LIMBO_WORLD_SPAWN",
					Value: b.Instance.Spec.World.SchematicWorldSpawn,
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
			SecurityContext: b.getSecurityContext(),
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
		})
	}

	return containers
}

func (b *MinecraftServerDeploymentDeploymentBuilder) getDeploymentInitFsEnv() []corev1.EnvVar {
	env := []corev1.EnvVar{
		{
			Name:  "SHULKER_CONFIG_DIR",
			Value: serverConfigDir,
		},
		{
			Name:  "SHULKER_DATA_DIR",
			Value: serverDataDir,
		},
	}

	return env
}

func (b *MinecraftServerDeploymentDeploymentBuilder) getDeploymentInitPluginsEnv() []corev1.EnvVar {
	return []corev1.EnvVar{
		{
			Name:  "SHULKER_DATA_DIR",
			Value: serverDataDir,
		},
		{
			Name:  "SHULKER_PLUGINS_URL",
			Value: strings.Join(b.Instance.Spec.Plugins, ";"),
		},
	}
}

func (b *MinecraftServerDeploymentDeploymentBuilder) getDeploymentEnv() []corev1.EnvVar {
	env := []corev1.EnvVar{
		{
			Name: "SERVER_ID",
			ValueFrom: &corev1.EnvVarSource{
				FieldRef: &corev1.ObjectFieldSelector{
					FieldPath: "metadata.name",
				},
			},
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
			Name:  "MAX_PLAYERS",
			Value: fmt.Sprintf("%d", *b.Instance.Spec.MaxPlayers),
		},
		{
			Name:  "MOTD",
			Value: b.Instance.Spec.Motd,
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
			Value: fmt.Sprintf("%dM", b.Instance.Spec.Resources.Limits.Memory().ScaledValue(resource.Mega)-256),
		},
		{
			Name:  "ONLINE_MODE",
			Value: "false",
		},
		{
			Name:  "OVERRIDE_SERVER_PROPERTIES",
			Value: "true",
		},
	}

	if b.Instance.Spec.Rcon.Enabled {
		rconSecretName := b.getRconSecretName()
		if b.Instance.Spec.Rcon.PasswordSecretName != "" {
			rconSecretName = b.Instance.Spec.Rcon.PasswordSecretName
		}

		env = append(env, []corev1.EnvVar{
			{
				Name:  "ENABLE_RCON",
				Value: strconv.FormatBool(b.Instance.Spec.Rcon.Enabled),
			},
			{
				Name:  "RCON_PORT",
				Value: "25575",
			},
			{
				Name: "RCON_PASSWORD",
				ValueFrom: &corev1.EnvVarSource{
					SecretKeyRef: &corev1.SecretKeySelector{
						LocalObjectReference: corev1.LocalObjectReference{
							Name: rconSecretName,
						},
						Key: "password",
					},
				},
			},
		}...)
	}

	if b.Instance.Spec.World != nil {
		if b.Instance.Spec.World.Url != "" {
			env = append(env, corev1.EnvVar{
				Name:  "WORLD",
				Value: b.Instance.Spec.World.Url,
			})
		}

		if b.Instance.Spec.World.SchematicUrl != "" {
			env = append(env, []corev1.EnvVar{
				{
					Name:  "LIMBO_SCHEMA_FILENAME",
					Value: "limbo.schematic",
				},
				{
					Name:  "LEVEL",
					Value: "world;limbo.schematic",
				},
			}...)
		}
	}

	env = append(env, b.Instance.Spec.PodOverrides.Env...)

	return env
}

func (b *MinecraftServerDeploymentDeploymentBuilder) getDeploymentPorts() []corev1.ContainerPort {
	ports := []corev1.ContainerPort{{
		ContainerPort: 25565,
		Name:          "minecraft",
	}}

	if b.Instance.Spec.Rcon.Enabled {
		ports = append(ports, corev1.ContainerPort{
			ContainerPort: 25575,
			Name:          "rcon",
		})
	}

	return ports
}

func (b *MinecraftServerDeploymentDeploymentBuilder) getSecurityContext() *corev1.SecurityContext {
	securityEscalation := false
	readOnlyFs := false // FIXME: true
	runAsNonRoot := true
	userUid := int64(1000)

	return &corev1.SecurityContext{
		AllowPrivilegeEscalation: &securityEscalation,
		ReadOnlyRootFilesystem:   &readOnlyFs,
		RunAsNonRoot:             &runAsNonRoot,
		RunAsUser:                &userUid,
		Capabilities: &corev1.Capabilities{
			Drop: []corev1.Capability{"ALL"},
		},
	}
}

func getTypeFromVersionChannel(channel shulkermciov1alpha1.MinecraftServerDeploymentVersionChannel) string {
	switch channel {
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionVanilla:
		return "VANILLA"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionForge:
		return "FORGE"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionFabric:
		return "FABRIC"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionSpigot:
		return "SPIGOT"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionPaper:
		return "PAPER"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionAirplace:
		return "AIRPLANE"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionPufferfish:
		return "PUFFERFISH"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionPurpur:
		return "PURPUR"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionMagma:
		return "MAGMA"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionMohist:
		return "MOHIST"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionCatserver:
		return "CATSERVER"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionCanyon:
		return "CANYON"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionSpongeVanilla:
		return "SPONGEVANILLA"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionLimbo:
		return "LIMBO"
	case shulkermciov1alpha1.MinecraftServerDeploymentVersionCrucible:
		return "CRUCIBLE"
	}

	return ""
}

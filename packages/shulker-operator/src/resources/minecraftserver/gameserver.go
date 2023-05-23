/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"
	"strings"

	agonesv1 "agones.dev/agones/pkg/apis/agones/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	shulkermciov1alpha1 "github.com/jeremylvln/shulker/packages/shulker-crds/v1alpha1"
	resources "github.com/jeremylvln/shulker/packages/shulker-resource-utils/src"
)

const minecraftServerShulkerConfigDir = "/mnt/shulker/config"
const minecraftServerConfigDir = "/config"
const minecraftServerDataDir = "/data"

type MinecraftServerResourceGameServerBuilder struct {
	*MinecraftServerResourceBuilder
}

func (b *MinecraftServerResourceBuilder) MinecraftServerGameServer() *MinecraftServerResourceGameServerBuilder {
	return &MinecraftServerResourceGameServerBuilder{b}
}

func (b *MinecraftServerResourceGameServerBuilder) Build() (client.Object, error) {
	return &agonesv1.GameServer{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.GetGameServerName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
			Annotations: map[string]string{
				"minecraftserver.shulkermc.io/tags": strings.Join(b.Instance.Spec.Tags, ","),
			},
		},
	}, nil
}

func (b *MinecraftServerResourceGameServerBuilder) Update(object client.Object) error {
	gameServer := object.(*agonesv1.GameServer)

	gameServerSpec, err := b.GetGameServerSpec()
	if err != nil {
		return err
	}

	gameServer.Spec = *gameServerSpec

	if err := controllerutil.SetControllerReference(b.Instance, gameServer, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for GameServer: %v", err)
	}

	return nil
}

func (b *MinecraftServerResourceGameServerBuilder) CanBeUpdated() bool {
	return true
}

func (b *MinecraftServerResourceGameServerBuilder) GetGameServerSpec() (*agonesv1.GameServerSpec, error) {
	initEnv, err := b.getInitEnv()
	if err != nil {
		return nil, err
	}

	podSpec := corev1.PodSpec{
		InitContainers: []corev1.Container{
			{
				Image:           "alpine:latest",
				Name:            "init-fs",
				Command:         []string{"sh", fmt.Sprintf("%s/init-fs.sh", minecraftServerShulkerConfigDir)},
				Env:             initEnv,
				SecurityContext: b.getSecurityContext(),
				VolumeMounts: []corev1.VolumeMount{
					{
						Name:      "shulker-config",
						MountPath: minecraftServerShulkerConfigDir,
						ReadOnly:  true,
					},
					{
						Name:      "server-config",
						MountPath: minecraftServerConfigDir,
					},
				},
			},
		},
		Containers: []corev1.Container{
			{
				Image: "itzg/minecraft-server:2022.16.0-java17",
				Name:  "minecraft-server",
				Env:   b.getEnv(),
				LivenessProbe: &corev1.Probe{
					ProbeHandler: corev1.ProbeHandler{
						Exec: &corev1.ExecAction{
							Command: []string{"bash", "/health.sh"},
						},
					},
					InitialDelaySeconds: 30,
					PeriodSeconds:       5,
					FailureThreshold:    5,
				},
				ReadinessProbe: &corev1.Probe{
					ProbeHandler: corev1.ProbeHandler{
						Exec: &corev1.ExecAction{
							Command: []string{"bash", "/health.sh"},
						},
					},
					InitialDelaySeconds: 30,
					PeriodSeconds:       5,
					FailureThreshold:    5,
				},
				ImagePullPolicy: corev1.PullIfNotPresent,
				SecurityContext: b.getSecurityContext(),
				VolumeMounts: []corev1.VolumeMount{
					{
						Name:      "server-config",
						MountPath: minecraftServerConfigDir,
					},
					{
						Name:      "server-data",
						MountPath: minecraftServerDataDir,
					},
					{
						Name:      "server-tmp",
						MountPath: "/tmp",
					},
				},
			},
		},
		ServiceAccountName: b.getServiceAccountName(),
		RestartPolicy:      corev1.RestartPolicyNever,
		Volumes: []corev1.Volume{
			{
				Name: "shulker-config",
				VolumeSource: corev1.VolumeSource{
					ConfigMap: &corev1.ConfigMapVolumeSource{
						LocalObjectReference: corev1.LocalObjectReference{
							Name: b.GetConfigMapName(),
						},
					},
				},
			},
			{
				Name: "server-config",
				VolumeSource: corev1.VolumeSource{
					EmptyDir: &corev1.EmptyDirVolumeSource{},
				},
			},
			{
				Name: "server-data",
				VolumeSource: corev1.VolumeSource{
					EmptyDir: &corev1.EmptyDirVolumeSource{},
				},
			},
			{
				Name: "server-tmp",
				VolumeSource: corev1.VolumeSource{
					EmptyDir: &corev1.EmptyDirVolumeSource{},
				},
			},
		},
	}

	if b.Instance.Spec.PodOverrides != nil {
		if b.Instance.Spec.PodOverrides.Image != nil {
			podSpec.Containers[0].Image = b.Instance.Spec.PodOverrides.Image.Name
			podSpec.Containers[0].ImagePullPolicy = b.Instance.Spec.PodOverrides.Image.PullPolicy
			podSpec.ImagePullSecrets = append(podSpec.ImagePullSecrets, b.Instance.Spec.PodOverrides.Image.PullSecrets...)
		}

		if b.Instance.Spec.PodOverrides.Resources != nil {
			podSpec.Containers[0].Resources = *b.Instance.Spec.PodOverrides.Resources
		}

		if b.Instance.Spec.PodOverrides.Affinity != nil {
			podSpec.Affinity = b.Instance.Spec.PodOverrides.Affinity
		}

		podSpec.NodeSelector = b.Instance.Spec.PodOverrides.NodeSelector
		podSpec.Tolerations = b.Instance.Spec.PodOverrides.Tolarations
	}

	gameServerSpec := agonesv1.GameServerSpec{
		Ports: []agonesv1.GameServerPort{{
			Name:          "minecraft",
			ContainerPort: 25565,
			Protocol:      corev1.ProtocolTCP,
		}},
		Eviction: &agonesv1.Eviction{
			Safe: agonesv1.EvictionSafeOnUpgrade,
		},
		Template: corev1.PodTemplateSpec{
			ObjectMeta: metav1.ObjectMeta{
				Labels: b.getLabels(),
			},
			Spec: podSpec,
		},
	}

	return &gameServerSpec, nil
}

func (b *MinecraftServerResourceGameServerBuilder) getInitEnv() ([]corev1.EnvVar, error) {
	resourceRefResolver := resources.ResourceRefResolver{
		Client:    b.Client,
		Ctx:       b.Ctx,
		Namespace: b.Instance.Namespace,
	}

	var worldUrl string
	var err error
	if b.Instance.Spec.Configuration.World != nil {
		worldUrl, err = resourceRefResolver.ResolveUrl(b.Instance.Spec.Configuration.World)
		if err != nil {
			return []corev1.EnvVar{}, nil
		}
	}

	var pluginUrls []string
	for _, ref := range b.Instance.Spec.Configuration.Plugins {
		pluginUrl, err := resourceRefResolver.ResolveUrl(&ref)
		if err != nil {
			return []corev1.EnvVar{}, nil
		}
		pluginUrls = append(pluginUrls, pluginUrl)
	}

	var patchesUrls []string
	for _, ref := range b.Instance.Spec.Configuration.Patches {
		patchUrl, err := resourceRefResolver.ResolveUrl(&ref)
		if err != nil {
			return []corev1.EnvVar{}, nil
		}
		patchesUrls = append(patchesUrls, patchUrl)
	}

	env := []corev1.EnvVar{
		{
			Name:  "SHULKER_CONFIG_DIR",
			Value: minecraftServerShulkerConfigDir,
		},
		{
			Name:  "SERVER_CONFIG_DIR",
			Value: minecraftServerConfigDir,
		},
		{
			Name:  "SERVER_DATA_DIR",
			Value: minecraftServerDataDir,
		},
		{
			Name:  "TYPE",
			Value: getTypeFromVersionChannel(b.Instance.Spec.Version.Channel),
		},
		{
			Name:  "SHULKER_SERVER_AGENT_VERSION",
			Value: "0.1.0",
		},
		{
			Name:  "SERVER_WORLD_URL",
			Value: worldUrl,
		},
		{
			Name:  "SERVER_PLUGIN_URLS",
			Value: strings.Join(pluginUrls, ";"),
		},
		{
			Name:  "SERVER_PATCH_URLS",
			Value: strings.Join(patchesUrls, ";"),
		},
	}

	return env, nil
}

func (b *MinecraftServerResourceGameServerBuilder) getEnv() []corev1.EnvVar {
	env := []corev1.EnvVar{
		{
			Name: "SHULKER_SERVER_NAME",
			ValueFrom: &corev1.EnvVarSource{
				FieldRef: &corev1.ObjectFieldSelector{
					FieldPath: "metadata.name",
				},
			},
		},
		{
			Name: "SHULKER_SERVER_NAMESPACE",
			ValueFrom: &corev1.EnvVarSource{
				FieldRef: &corev1.ObjectFieldSelector{
					FieldPath: "metadata.namespace",
				},
			},
		},
		{
			Name:  "TYPE",
			Value: getTypeFromVersionChannel(b.Instance.Spec.Version.Channel),
		},
		{
			Name:  getVersionEnvFromVersionChannel(b.Instance.Spec.Version.Channel),
			Value: b.Instance.Spec.Version.Name,
		},
		{
			Name:  "EULA",
			Value: "TRUE",
		},
		{
			Name:  "COPY_CONFIG_DEST",
			Value: minecraftServerDataDir,
		},
		{
			Name:  "SYNC_SKIP_NEWER_IN_DESTINATION",
			Value: "false",
		},
		{
			Name:  "SKIP_SERVER_PROPERTIES",
			Value: "true",
		},
		{
			Name:  "REPLACE_ENV_IN_PLACE",
			Value: "true",
		},
		{
			Name:  "REPLACE_ENV_VARIABLE_PREFIX",
			Value: "CFG_",
		},
		{
			Name: "CFG_VELOCITY_FORWARDING_SECRET",
			ValueFrom: &corev1.EnvVarSource{
				SecretKeyRef: &corev1.SecretKeySelector{
					LocalObjectReference: corev1.LocalObjectReference{
						Name: fmt.Sprintf("%s-forwarding-secret", b.Instance.Spec.ClusterRef.Name),
					},
					Key: "key",
				},
			},
		},
		{
			Name:  "MEMORY",
			Value: "",
		},
		{
			Name:  "JVM_XX_OPTS",
			Value: "-XX:MaxRAMPercentage=75",
		},
	}

	if b.Instance.Spec.PodOverrides != nil {
		env = append(env, b.Instance.Spec.PodOverrides.Env...)
	}

	return env
}

func (b *MinecraftServerResourceGameServerBuilder) getSecurityContext() *corev1.SecurityContext {
	securityEscalation := false
	readOnlyFs := true
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

func getTypeFromVersionChannel(channel shulkermciov1alpha1.MinecraftServerVersionChannel) string {
	switch channel {
	case shulkermciov1alpha1.MinecraftServerVersionPaper:
		return "PAPER"
	case shulkermciov1alpha1.MinecraftServerVersionBukkit:
		return "BUKKIT"
	case shulkermciov1alpha1.MinecraftServerVersionSpigot:
		return "SPIGOT"
	case shulkermciov1alpha1.MinecraftServerVersionPufferfish:
		return "PUFFERFISH"
	case shulkermciov1alpha1.MinecraftServerVersionForge:
		return "FORGE"
	case shulkermciov1alpha1.MinecraftServerVersionFabric:
		return "FABRIC"
	case shulkermciov1alpha1.MinecraftServerVersionQuilt:
		return "QUILT"
	}

	return ""
}

func getVersionEnvFromVersionChannel(channel shulkermciov1alpha1.MinecraftServerVersionChannel) string {
	return "VERSION"
}

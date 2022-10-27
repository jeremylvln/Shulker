/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
)

const minecraftServerShulkerConfigDir = "/mnt/shulker/config"
const minecraftServerConfigDir = "/config"
const minecraftServerDataDir = "/data"

type MinecraftServerResourcePodBuilder struct {
	*MinecraftServerResourceBuilder
}

func (b *MinecraftServerResourceBuilder) MinecraftServerPod() *MinecraftServerResourcePodBuilder {
	return &MinecraftServerResourcePodBuilder{b}
}

func (b *MinecraftServerResourcePodBuilder) Build() (client.Object, error) {
	return &corev1.Pod{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.GetPodName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *MinecraftServerResourcePodBuilder) Update(object client.Object) error {
	pod := object.(*corev1.Pod)

	pod.Spec = corev1.PodSpec{
		InitContainers: []corev1.Container{
			{
				Image:   "alpine:latest",
				Name:    "init-fs",
				Command: []string{"sh", fmt.Sprintf("%s/init-fs.sh", minecraftServerShulkerConfigDir)},
				Env: []corev1.EnvVar{
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
				},
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
				Image: "itzg/minecraft-server:latest",
				Name:  "minecraft-server",
				Ports: []corev1.ContainerPort{{
					Name:          "minecraft",
					ContainerPort: 25565,
				}},
				Env: b.getEnv(),
				LivenessProbe: &corev1.Probe{
					ProbeHandler: corev1.ProbeHandler{
						Exec: &corev1.ExecAction{
							Command: []string{"bash", "/health.sh"},
						},
					},
					InitialDelaySeconds: 60,
					PeriodSeconds:       10,
				},
				ReadinessProbe: &corev1.Probe{
					ProbeHandler: corev1.ProbeHandler{
						Exec: &corev1.ExecAction{
							Command: []string{"bash", "/health.sh"},
						},
					},
					InitialDelaySeconds: 60,
					PeriodSeconds:       10,
				},
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
		if b.Instance.Spec.PodOverrides.Affinity != nil {
			pod.Spec.Affinity = b.Instance.Spec.PodOverrides.Affinity
		}
	}

	if err := controllerutil.SetControllerReference(b.Instance, pod, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Pod: %v", err)
	}

	return nil
}

func (b *MinecraftServerResourcePodBuilder) CanBeUpdated() bool {
	return false
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

func (b *MinecraftServerResourcePodBuilder) getEnv() []corev1.EnvVar {
	env := []corev1.EnvVar{
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
	}

	return env
}

func (b *MinecraftServerResourcePodBuilder) getSecurityContext() *corev1.SecurityContext {
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

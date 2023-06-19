/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package resources

import (
	"fmt"
	"strconv"
	"strings"

	agonesapis "agones.dev/agones/pkg/apis"
	agonesv1 "agones.dev/agones/pkg/apis/agones/v1"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	shulkermciov1alpha1 "github.com/jeremylvln/shulker/packages/shulker-crds/v1alpha1"
	resources "github.com/jeremylvln/shulker/packages/shulker-resource-utils/src"
)

const proxyShulkerConfigDir = "/mnt/shulker/config"
const proxyShulkerForwardingSecretDir = "/mnt/shulker/forwarding-secret"
const proxyDataDir = "/server"
const proxyDrainLockDir = "/mnt/drain-lock"

type ProxyFleetResourceFleetBuilder struct {
	*ProxyFleetResourceBuilder
}

func (b *ProxyFleetResourceBuilder) ProxyFleetFleet() *ProxyFleetResourceFleetBuilder {
	return &ProxyFleetResourceFleetBuilder{b}
}

func (b *ProxyFleetResourceFleetBuilder) Build() (client.Object, error) {
	return &agonesv1.Fleet{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.GetFleetName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *ProxyFleetResourceFleetBuilder) Update(object client.Object) error {
	fleet := object.(*agonesv1.Fleet)

	gameServerSpec, err := b.getGameServerSpec()
	if err != nil {
		return err
	}

	fleet.Spec = agonesv1.FleetSpec{
		Replicas: int32(b.Instance.Spec.Replicas),
		Strategy: appsv1.DeploymentStrategy{
			Type: appsv1.RollingUpdateDeploymentStrategyType,
			RollingUpdate: &appsv1.RollingUpdateDeployment{
				MaxUnavailable: &intstr.IntOrString{
					Type:   intstr.String,
					StrVal: "25%",
				},
				MaxSurge: &intstr.IntOrString{
					Type:   intstr.String,
					StrVal: "25%",
				},
			},
		},
		Scheduling: agonesapis.Packed,
		Template: agonesv1.GameServerTemplateSpec{
			ObjectMeta: metav1.ObjectMeta{
				Labels: b.getLabels(),
			},
			Spec: *gameServerSpec,
		},
	}

	if err := controllerutil.SetControllerReference(b.Instance, fleet, b.Scheme); err != nil {
		return fmt.Errorf("failed setting controller reference for Fleet: %v", err)
	}

	return nil
}

func (b *ProxyFleetResourceFleetBuilder) CanBeUpdated() bool {
	return true
}

func (b *ProxyFleetResourceFleetBuilder) getGameServerSpec() (*agonesv1.GameServerSpec, error) {
	initEnv, err := b.getInitEnv()
	if err != nil {
		return nil, err
	}

	podSpec := corev1.PodSpec{
		InitContainers: []corev1.Container{
			{
				Image:           "alpine:latest",
				Name:            "init-fs",
				Command:         []string{"sh", fmt.Sprintf("%s/init-fs.sh", proxyShulkerConfigDir)},
				Env:             initEnv,
				SecurityContext: b.getSecurityContext(),
				VolumeMounts: []corev1.VolumeMount{
					{
						Name:      "shulker-config",
						MountPath: proxyShulkerConfigDir,
						ReadOnly:  true,
					},
					{
						Name:      "proxy-data",
						MountPath: proxyDataDir,
					},
				},
			},
		},
		Containers: []corev1.Container{
			{
				Image: "itzg/bungeecord:java17-2022.4.1",
				Name:  "proxy",
				Ports: []corev1.ContainerPort{{
					Name:          "minecraft",
					ContainerPort: 25577,
				}},
				Env: b.getEnv(),
				LivenessProbe: &corev1.Probe{
					ProbeHandler: corev1.ProbeHandler{
						Exec: &corev1.ExecAction{
							Command: []string{"bash", "/health.sh"},
						},
					},
					InitialDelaySeconds: 10,
					PeriodSeconds:       10,
				},
				ReadinessProbe: &corev1.Probe{
					ProbeHandler: corev1.ProbeHandler{
						Exec: &corev1.ExecAction{
							Command: []string{"bash", fmt.Sprintf("%s/probe-readiness.sh", proxyDataDir)},
						},
					},
					InitialDelaySeconds: 10,
					PeriodSeconds:       10,
				},
				ImagePullPolicy: corev1.PullIfNotPresent,
				SecurityContext: b.getSecurityContext(),
				VolumeMounts: []corev1.VolumeMount{
					{
						Name:      "shulker-forwarding-secret",
						MountPath: proxyShulkerForwardingSecretDir,
						ReadOnly:  true,
					},
					{
						Name:      "proxy-data",
						MountPath: proxyDataDir,
					},
					{
						Name:      "proxy-drain-lock",
						MountPath: proxyDrainLockDir,
						ReadOnly:  true,
					},
					{
						Name:      "proxy-tmp",
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
				Name: "shulker-forwarding-secret",
				VolumeSource: corev1.VolumeSource{
					Secret: &corev1.SecretVolumeSource{
						SecretName: fmt.Sprintf("%s-forwarding-secret", b.Instance.Spec.ClusterRef.Name),
					},
				},
			},
			{
				Name: "proxy-data",
				VolumeSource: corev1.VolumeSource{
					EmptyDir: &corev1.EmptyDirVolumeSource{},
				},
			},
			{
				Name: "proxy-drain-lock",
				VolumeSource: corev1.VolumeSource{
					EmptyDir: &corev1.EmptyDirVolumeSource{},
				},
			},
			{
				Name: "proxy-tmp",
				VolumeSource: corev1.VolumeSource{
					EmptyDir: &corev1.EmptyDirVolumeSource{},
				},
			},
		},
	}

	if b.Instance.Spec.Template.Spec.PodOverrides != nil {
		if b.Instance.Spec.Template.Spec.PodOverrides.Image != nil {
			podSpec.Containers[0].Image = b.Instance.Spec.Template.Spec.PodOverrides.Image.Name
			podSpec.Containers[0].ImagePullPolicy = b.Instance.Spec.Template.Spec.PodOverrides.Image.PullPolicy
			podSpec.ImagePullSecrets = append(podSpec.ImagePullSecrets, b.Instance.Spec.Template.Spec.PodOverrides.Image.PullSecrets...)
		}

		if b.Instance.Spec.Template.Spec.PodOverrides.Resources != nil {
			podSpec.Containers[0].Resources = *b.Instance.Spec.Template.Spec.PodOverrides.Resources
		}

		if b.Instance.Spec.Template.Spec.PodOverrides.Affinity != nil {
			podSpec.Affinity = b.Instance.Spec.Template.Spec.PodOverrides.Affinity
		}

		podSpec.NodeSelector = b.Instance.Spec.Template.Spec.PodOverrides.NodeSelector
		podSpec.Tolerations = b.Instance.Spec.Template.Spec.PodOverrides.Tolarations
	}

	gameServerSpec := agonesv1.GameServerSpec{
		// Ports: []agonesv1.GameServerPort{{
		// 	Name:          "minecraft",
		// 	ContainerPort: 25565,
		// 	Protocol:      corev1.ProtocolTCP,
		// }},
		Ports: []agonesv1.GameServerPort{},
		Eviction: &agonesv1.Eviction{
			Safe: agonesv1.EvictionSafeOnUpgrade,
		},
		Template: corev1.PodTemplateSpec{
			ObjectMeta: metav1.ObjectMeta{
				Labels: b.Instance.Spec.Template.ObjectMeta.Labels,
			},
			Spec: podSpec,
		},
	}

	return &gameServerSpec, nil
}

func (b *ProxyFleetResourceFleetBuilder) getInitEnv() ([]corev1.EnvVar, error) {
	resourceRefResolver := resources.ResourceRefResolver{
		Client:    b.Client,
		Ctx:       b.Ctx,
		Namespace: b.Instance.Namespace,
	}

	var pluginUrls []string
	for _, ref := range b.Instance.Spec.Template.Spec.Configuration.Plugins {
		pluginUrl, err := resourceRefResolver.ResolveUrl(&ref)
		if err != nil {
			return []corev1.EnvVar{}, nil
		}
		pluginUrls = append(pluginUrls, pluginUrl)
	}

	var patchesUrls []string
	for _, ref := range b.Instance.Spec.Template.Spec.Configuration.Patches {
		patchUrl, err := resourceRefResolver.ResolveUrl(&ref)
		if err != nil {
			return []corev1.EnvVar{}, nil
		}
		patchesUrls = append(patchesUrls, patchUrl)
	}

	env := []corev1.EnvVar{
		{
			Name:  "SHULKER_CONFIG_DIR",
			Value: proxyShulkerConfigDir,
		},
		{
			Name:  "PROXY_DATA_DIR",
			Value: proxyDataDir,
		},
		{
			Name:  "TYPE",
			Value: getTypeFromVersionChannel(b.Instance.Spec.Template.Spec.Version.Channel),
		},
		{
			Name:  "SHULKER_PROXY_AGENT_VERSION",
			Value: "0.1.0",
		},
		{
			Name:  "PROXY_PLUGIN_URLS",
			Value: strings.Join(pluginUrls, ";"),
		},
		{
			Name:  "PROXY_PATCH_URLS",
			Value: strings.Join(patchesUrls, ";"),
		},
	}

	return env, nil
}

func (b *ProxyFleetResourceFleetBuilder) getEnv() []corev1.EnvVar {
	env := []corev1.EnvVar{
		{
			Name: "SHULKER_PROXY_NAME",
			ValueFrom: &corev1.EnvVarSource{
				FieldRef: &corev1.ObjectFieldSelector{
					FieldPath: "metadata.name",
				},
			},
		},
		{
			Name: "SHULKER_PROXY_NAMESPACE",
			ValueFrom: &corev1.EnvVarSource{
				FieldRef: &corev1.ObjectFieldSelector{
					FieldPath: "metadata.namespace",
				},
			},
		},
		{
			Name:  "SHULKER_PROXY_TTL_SECONDS",
			Value: fmt.Sprintf("%d", b.Instance.Spec.Template.Spec.Configuration.TimeToLiveSeconds),
		},
		{
			Name:  "TYPE",
			Value: getTypeFromVersionChannel(b.Instance.Spec.Template.Spec.Version.Channel),
		},
		{
			Name:  getVersionEnvFromVersionChannel(b.Instance.Spec.Template.Spec.Version.Channel),
			Value: b.Instance.Spec.Template.Spec.Version.Name,
		},
		{
			Name:  "HEALTH_USE_PROXY",
			Value: strconv.FormatBool(b.Instance.Spec.Template.Spec.Configuration.ProxyProtocol),
		},
	}

	if b.Instance.Spec.Template.Spec.PodOverrides != nil {
		env = append(env, b.Instance.Spec.Template.Spec.PodOverrides.Env...)
	}

	return env
}

func (b *ProxyFleetResourceFleetBuilder) getSecurityContext() *corev1.SecurityContext {
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

func getTypeFromVersionChannel(channel shulkermciov1alpha1.ProxyVersionChannel) string {
	switch channel {
	case shulkermciov1alpha1.ProxyFleetVersionBungeeCord:
		return "BUNGEECORD"
	case shulkermciov1alpha1.ProxyFleetVersionWaterfall:
		return "WATERFALL"
	case shulkermciov1alpha1.ProxyFleetVersionVelocity:
		return "VELOCITY"
	}

	return ""
}

func getVersionEnvFromVersionChannel(channel shulkermciov1alpha1.ProxyVersionChannel) string {
	switch channel {
	case shulkermciov1alpha1.ProxyFleetVersionBungeeCord:
		return "BUNGEE_JOB_ID"
	case shulkermciov1alpha1.ProxyFleetVersionWaterfall:
		return "WATERFALL_BUILD_ID"
	case shulkermciov1alpha1.ProxyFleetVersionVelocity:
		return "VELOCITY_BUILD_ID"
	}

	return ""
}

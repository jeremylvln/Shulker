package resource

import (
	"fmt"

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

const proxyConfigDir = "/config"
const proxyServerDir = "/server"

type ProxyDeploymentDeploymentBuilder struct {
	*ProxyDeploymentResourceBuilder
}

func (b *ProxyDeploymentResourceBuilder) ProxyDeploymentDeployment() *ProxyDeploymentDeploymentBuilder {
	return &ProxyDeploymentDeploymentBuilder{b}
}

func (b *ProxyDeploymentDeploymentBuilder) Build() (client.Object, error) {
	return &appsv1.Deployment{
		ObjectMeta: metav1.ObjectMeta{
			Name:      b.GetDeploymentName(),
			Namespace: b.Instance.Namespace,
			Labels:    b.getLabels(),
		},
	}, nil
}

func (b *ProxyDeploymentDeploymentBuilder) Update(object client.Object) error {
	deployment := object.(*appsv1.Deployment)
	revisionHistoryLimit := int32(1)

	deployment.Spec = appsv1.DeploymentSpec{
		Replicas: &b.Instance.Spec.Replicas,
		Selector: &metav1.LabelSelector{
			MatchLabels: b.getLabels(),
		},
		Template: corev1.PodTemplateSpec{
			ObjectMeta: metav1.ObjectMeta{
				Labels: b.getLabels(),
			},
			Spec: corev1.PodSpec{
				InitContainers: []corev1.Container{
					{
						Image:   "busybox:stable",
						Name:    "init-proxy-fs",
						Command: []string{"ash", fmt.Sprintf("%s/init-fs.sh", proxyConfigDir)},
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
						VolumeMounts: []corev1.VolumeMount{
							{
								Name:      "proxy-server-dir",
								MountPath: proxyServerDir,
							},
							{
								Name:      "proxy-config-dir",
								MountPath: proxyConfigDir,
								ReadOnly:  true,
							},
						},
					},
					{
						Image:   "curlimages/curl:latest",
						Name:    "init-proxy-plugins",
						Command: []string{"ash", fmt.Sprintf("%s/init-plugins.sh", proxyConfigDir)},
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
						VolumeMounts: []corev1.VolumeMount{
							{
								Name:      "proxy-server-dir",
								MountPath: proxyServerDir,
							},
							{
								Name:      "proxy-config-dir",
								MountPath: proxyConfigDir,
								ReadOnly:  true,
							},
						},
					},
				},
				Containers: []corev1.Container{{
					Image: "itzg/bungeecord:latest",
					Name:  "proxy",
					Ports: []corev1.ContainerPort{{
						ContainerPort: 25577,
						Name:          "minecraft",
					}},
					Env: b.getDeploymentEnv(),
					LivenessProbe: &corev1.Probe{
						ProbeHandler: corev1.ProbeHandler{
							TCPSocket: &corev1.TCPSocketAction{
								Port: intstr.FromInt(25577),
							},
						},
						InitialDelaySeconds: b.Instance.Spec.PodOverrides.LivenessProbe.InitialDelaySeconds,
						PeriodSeconds:       10,
					},
					ReadinessProbe: &corev1.Probe{
						ProbeHandler: corev1.ProbeHandler{
							TCPSocket: &corev1.TCPSocketAction{
								Port: intstr.FromInt(25577),
							},
						},
						InitialDelaySeconds: b.Instance.Spec.PodOverrides.ReadinessProbe.InitialDelaySeconds,
						PeriodSeconds:       10,
					},
					Resources: *b.Instance.Spec.Resources,
					VolumeMounts: []corev1.VolumeMount{
						{
							Name:      "proxy-server-dir",
							MountPath: proxyServerDir,
						},
					},
				}},
				ServiceAccountName:            b.getServiceAccountName(),
				TerminationGracePeriodSeconds: b.Instance.Spec.PodOverrides.TerminationGracePeriodSeconds,
				Affinity:                      b.Instance.Spec.Affinity,
				Volumes: []corev1.Volume{
					{
						Name: "proxy-server-dir",
						VolumeSource: corev1.VolumeSource{
							EmptyDir: &corev1.EmptyDirVolumeSource{},
						},
					},
					{
						Name: "proxy-config-dir",
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

func (b *ProxyDeploymentDeploymentBuilder) CanBeUpdated() bool {
	return true
}

func (b *ProxyDeploymentDeploymentBuilder) getDeploymentInitFsEnv() []corev1.EnvVar {
	env := []corev1.EnvVar{
		{
			Name:  "SHULKER_CONFIG_DIR",
			Value: proxyConfigDir,
		},
		{
			Name:  "SHULKER_DATA_DIR",
			Value: proxyServerDir,
		},
	}

	return env
}

func (b *ProxyDeploymentDeploymentBuilder) getDeploymentInitPluginsEnv() []corev1.EnvVar {
	env := []corev1.EnvVar{
		{
			Name:  "SHULKER_DATA_DIR",
			Value: proxyServerDir,
		},
		{
			Name: "SHULKER_MAVEN_USERNAME",
			ValueFrom: &corev1.EnvVarSource{
				SecretKeyRef: &corev1.SecretKeySelector{
					LocalObjectReference: corev1.LocalObjectReference{
						Name: b.Cluster.Spec.MavenSecretName,
					},
					Key: "username",
				},
			},
		},
		{
			Name: "SHULKER_MAVEN_PASSWORD",
			ValueFrom: &corev1.EnvVarSource{
				SecretKeyRef: &corev1.SecretKeySelector{
					LocalObjectReference: corev1.LocalObjectReference{
						Name: b.Cluster.Spec.MavenSecretName,
					},
					Key: "password",
				},
			},
		},
		{
			Name:  "SHULKER_PROXY_DIRECTORY_VERSION",
			Value: "0.0.1",
		},
	}

	return env
}

func (b *ProxyDeploymentDeploymentBuilder) getDeploymentEnv() []corev1.EnvVar {
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
			Name:  "INIT_MEMORY",
			Value: fmt.Sprintf("%dM", b.Instance.Spec.Resources.Requests.Memory().ScaledValue(resource.Mega)),
		},
		{
			Name:  "MAX_MEMORY",
			Value: fmt.Sprintf("%dM", b.Instance.Spec.Resources.Limits.Memory().ScaledValue(resource.Mega)-1000),
		},
		{
			Name:  "SHULKER_CLUSTER_NAMESPACE",
			Value: b.Cluster.Namespace,
		},
		{
			Name:  "SHULKER_CLUSTER_NAME",
			Value: b.Cluster.Name,
		},
	}

	env = append(env, b.Instance.Spec.PodOverrides.Env...)

	return env
}

func getTypeFromVersionChannel(channel shulkermciov1alpha1.ProxyDeploymentVersionChannel) string {
	switch channel {
	case shulkermciov1alpha1.ProxyDeploymentVersionBungeeCord:
		return "BUNGEECORD"
	case shulkermciov1alpha1.ProxyDeploymentVersionWaterfall:
		return "WATERFALL"
	case shulkermciov1alpha1.ProxyDeploymentVersionVelocity:
		return "VELOCITY"
	}

	return ""
}

func getVersionEnvFromVersionChannel(channel shulkermciov1alpha1.ProxyDeploymentVersionChannel) string {
	switch channel {
	case shulkermciov1alpha1.ProxyDeploymentVersionBungeeCord:
		return "BUNGEE_JOB_ID"
	case shulkermciov1alpha1.ProxyDeploymentVersionWaterfall:
		return "WATERFALL_VERSION"
	case shulkermciov1alpha1.ProxyDeploymentVersionVelocity:
		return "VELOCITY"
	}

	return ""
}

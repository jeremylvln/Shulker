/*
Copyright 2022.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package v1alpha1

import (
	corev1 "k8s.io/api/core/v1"
	meta "k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status
//+kubebuilder:subresource:scale:specpath=.spec.replicas,statuspath=.status.replicas,selectorpath=.status.selector
//+kubebuilder:printcolumn:name="Ready",type="boolean",JSONPath=".status.conditions[?(@.type==\"Ready\")].status"
//+kubebuilder:printcolumn:name="Replicas",type="date",JSONPath=".status.replicas"
//+kubebuilder:printcolumn:name="Available Replicas",type="date",JSONPath=".status.availableReplicas"
//+kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"
//+kubebuilder:resource:shortName={"spd"},categories=all

// ProxyDeployment is the Schema for the ProxyDeployment API
type ProxyDeployment struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   ProxyDeploymentSpec   `json:"spec,omitempty"`
	Status ProxyDeploymentStatus `json:"status,omitempty"`
}

// ProxyDeploymentSpec defines the desired state of ProxyDeployment
type ProxyDeploymentSpec struct {
	// Reference to a Minecraft Cluster. Adding this will enroll
	// this Proxy Deployment to be part of a Minecraft Cluster.
	//+kubebuilder:validation:Required
	ClusterRef MinecraftClusterRef `json:"minecraftClusterRef"`

	// Version the Proxy Deployment has to run.
	//+kubebuilder:validation:Required
	Version ProxyDeploymentVersionSpec `json:"version,omitempty"`

	// Number of replicas the Proxy Deployment should have.
	//+kubebuilder:validation:Required
	Replicas int32 `json:"replicas,omitempty"`

	// Number of maximum players that can connect to the
	// Proxy Deployment.
	//+kubebuilder:default=100
	MaxPlayers *int64 `json:"maxPlayers,omitempty"`

	// Message to display when the players query the status
	// of the Proxy Deployment.
	//+kubebuilder:default="A Minecraft Cluster on Shulker"
	Motd string `json:"motd,omitempty"`

	// Server icon image in base64 format.
	ServerIcon string `json:"serverIcon,omitempty"`

	// A list of download URLs for plugins.
	Plugins []string `json:"plugins,omitempty"`

	// The desired state of the Kubernetes Service to create for the
	// Proxy Deployment.
	//+kubebuilder:default={enabled: true, type: "LoadBalancer"}
	Service *ProxyDeploymentServiceSpec `json:"service,omitempty"`

	// Overrides configuration for the Proxy Deployment pods.
	//+kubebuilder:default={livenessProbe: {initialDelaySeconds: 15}, readinessProbe: {initialDelaySeconds: 15}, terminationGracePeriodSeconds: 3600}
	PodOverrides *ProxyDeploymentPodOverridesSpec `json:"podOverrides,omitempty"`

	// The desired compute resource requirements of Pods in the Proxy
	// Deployment.
	//+kubebuilder:default={limits: {cpu: "2000m", memory: "2Gi"}, requests: {cpu: "1000m", memory: "1Gi"}}
	Resources *corev1.ResourceRequirements `json:"resources,omitempty"`

	// Affinity scheduling rules to be applied on created Pods.
	Affinity *corev1.Affinity `json:"affinity,omitempty"`

	// Number of pods to configure in the Pod Disruption Budget of
	// the Proxy Deployment.
	//+kubebuilder:default={enabled: true, minAvailable: 1}
	DisruptionBudget *ProxyDeploymentDisruptionBudgetSpec `json:"disruptionBudget,omitempty"`
}

//+kubebuilder:validation:Enum=BungeeCord;Waterfall;Velocity
type ProxyDeploymentVersionChannel string

const (
	ProxyDeploymentVersionBungeeCord ProxyDeploymentVersionChannel = "BungeeCord"
	ProxyDeploymentVersionWaterfall  ProxyDeploymentVersionChannel = "Waterfall"
	ProxyDeploymentVersionVelocity   ProxyDeploymentVersionChannel = "Velocity"
)

// Defines the version of Minecraft to run on the server.
// The version can come from a channel which allows the user
// to run a version different from Mojang's Vanilla.
type ProxyDeploymentVersionSpec struct {
	// Channel of the version to use. Defaults to BungeeCord.
	//+optional
	//+kubebuilder:default=BungeeCord
	Channel ProxyDeploymentVersionChannel `json:"channel,omitempty"`

	// Name of the version to use.
	//+kubebuilder:validation:Required
	Name string `json:"name"`
}

// Configuration attributes for the Service resource.
type ProxyDeploymentServiceSpec struct {
	// Whether to create a Service for the Proxy Deployment. Defaults
	// to false.
	//+kubebuilder:default=false
	Enabled bool `json:"enabled,omitempty"`

	// Type of Service to create. Must be one of: ClusterIP, LoadBalancer, NodePort.
	// More info: https://pkg.go.dev/k8s.io/api/core/v1#ServiceType
	//+kubebuilder:validation:Enum=ClusterIP;LoadBalancer;NodePort
	//+kubebuilder:default="ClusterIP"
	Type corev1.ServiceType `json:"type,omitempty"`

	// Annotations to add to the Service.
	Annotations map[string]string `json:"annotations,omitempty"`
}

type ProxyDeploymentPodOverridesSpec struct {
	// Additional environment variables to add to the Proxy
	// Deployment.
	Env []corev1.EnvVar `json:"env,omitempty"`

	// Overrides for the liveness probe of the Proxy Deployment.
	LivenessProbe *ProxyDeploymentPodProbeSpec `json:"livenessProbe,omitempty"`

	// Overrides for the readiness probe of the Proxy Deployment.
	ReadinessProbe *ProxyDeploymentPodProbeSpec `json:"readinessProbe,omitempty"`

	// Number of seconds before force killing the pod after a graceful
	// termination request. Defaults to 1 minute.
	//+kubebuilder:default=3600
	TerminationGracePeriodSeconds *int64 `json:"terminationGracePeriodSeconds,omitempty"`
}

type ProxyDeploymentPodProbeSpec struct {
	// Number of seconds before starting to perform the probe. Depending
	// on the server configuration, one can take more time than another to
	// be ready. Defaults to 15 seconds.
	//+kubebuilder:default=15
	InitialDelaySeconds int32 `json:"initialDelaySeconds,omitempty"`
}

type ProxyDeploymentDisruptionBudgetSpec struct {
	// Whether to enable the creation of a Pod Disruption Budget
	// object for the Proxy Deployment. Defaults to true.
	//+kubebuilder:default=true
	Enabled bool `json:"enabled,omitempty"`

	// Minimal amount of pods needed to be available at any time.
	//+kubebuilder:default=1
	MinAvailable *int32 `json:"minAvailable,omitempty"`

	// Maximum amount of pods that can be unailable at the same
	// time.
	MaxUnavailable *int32 `json:"maxUnavailable,omitempty"`
}

type ProxyDeploymentStatusCondition string

const (
	ProxyDeploymentReadyCondition       ProxyDeploymentStatusCondition = "Ready"
	ProxyDeploymentAvailableCondition   ProxyDeploymentStatusCondition = "Available"
	ProxyDeploymentProgressingCondition ProxyDeploymentStatusCondition = "Progressing"
)

// ProxyDeploymentStatus defines the observed state of ProxyDeployment
type ProxyDeploymentStatus struct {
	// Conditions represent the latest available observations of a
	// ProxyDeployment object.
	//+kubebuilder:validation:Required
	Conditions []metav1.Condition `json:"conditions"`

	// Number of total replicas in Proxy Deployment.
	Replicas int32 `json:"replicas"`

	// Number of available replicas in Proxy Deployment.
	AvailableReplicas int32 `json:"availableReplicas"`

	// Number of unavailable replicas in Proxy Deployment.
	UnavailableReplicas int32 `json:"unavailableReplicas"`

	// Pod label selector.
	Selector string `json:"selector"`
}

func (s *ProxyDeploymentStatus) SetCondition(condition ProxyDeploymentStatusCondition, status metav1.ConditionStatus, reason string, message string) {
	meta.SetStatusCondition(&s.Conditions, metav1.Condition{
		Type:    string(condition),
		Status:  status,
		Reason:  reason,
		Message: message,
	})
}

//+kubebuilder:object:root=true

// ProxyDeploymentList contains a list of ProxyDeployment
type ProxyDeploymentList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []ProxyDeployment `json:"items"`
}

func init() {
	SchemeBuilder.Register(&ProxyDeployment{}, &ProxyDeploymentList{})
}

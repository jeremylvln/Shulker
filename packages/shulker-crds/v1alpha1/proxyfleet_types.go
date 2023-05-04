/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package v1alpha1

import (
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

const ProxyFleetDrainAnnotationName = "proxy.shulkermc.io/drain"

// ProxyFleetSpec defines the desired state of ProxyFleet
type ProxyFleetSpec struct {
	// Reference to a MinecraftCluster. Adding this will enroll
	// this ProxyFleet to be part of a MinecraftCluster.
	//+kubebuilder:validation:Required
	ClusterRef MinecraftClusterRef `json:"clusterRef,omitempty"`

	// Number of Proxy replicas to create.
	//+kubebuilder:validation:Required
	Replicas int32 `json:"replicas"`

	// The desired state of the Kubernetes Service to create for the
	// Proxy Deployment.
	//+optional
	Service ProxyFleetServiceSpec `json:"service,omitempty"`

	// Defines the version of the proxy to run.
	// The version can come from a channel which allows the user
	// to run a version different from the default BungeeCord.
	//+kubebuilder:validation:Required
	Version ProxyFleetVersionSpec `json:"version"`

	// Custom configuration flags to custom the proxy behavior.
	//+kubebuilder:default={}
	Configuration ProxyFleetConfigurationSpec `json:"config,omitempty"`

	// Overrides for values to be injected in the created Pod
	// of this ProxyFleet.
	PodOverrides *ProxyFleetPodOverridesSpec `json:"podOverrides,omitempty"`
}

// Configuration attributes for the Service resource.
type ProxyFleetServiceSpec struct {
	// Type of Service to create. Must be one of: ClusterIP, LoadBalancer, NodePort.
	//+kubebuilder:validation:Enum=ClusterIP;LoadBalancer;NodePort
	//+kubebuilder:default="LoadBalancer"
	Type corev1.ServiceType `json:"type,omitempty"`

	// Annotations to add to the Service.
	Annotations map[string]string `json:"annotations,omitempty"`

	// Describe how nodes distribute service traffic to the proxy.
	//+kubebuilder:validation:Enum=Cluster;Local
	//+kubebuilder:default="Cluster"
	ExternalTrafficPolicy corev1.ServiceExternalTrafficPolicyType `json:"externalTrafficPolicy,omitempty"`
}

// +kubebuilder:validation:Enum=BungeeCord;Waterfall;Velocity
type ProxyFleetVersionChannel string

const (
	ProxyFleetVersionBungeeCord ProxyFleetVersionChannel = "BungeeCord"
	ProxyFleetVersionWaterfall  ProxyFleetVersionChannel = "Waterfall"
	ProxyFleetVersionVelocity   ProxyFleetVersionChannel = "Velocity"
)

// Defines the version of the proxy to run.
type ProxyFleetVersionSpec struct {
	// Channel of the version to use. Defaults to BungeeCord.
	//+optional
	//+kubebuilder:default=Velocity
	Channel ProxyFleetVersionChannel `json:"channel,omitempty"`

	// Name of the version to use.
	//+kubebuilder:validation:Required
	Name string `json:"name"`
}

type ProxyFleetConfigurationSpec struct {
	// Name of an optional ConfigMap already containing the proxy
	// configuration.
	//+optional
	ExistingConfigMapName string `json:"existingConfigMapName,omitempty"`

	// List of references to plugins to download.
	//+optional
	Plugins []ResourceRef `json:"plugins,omitempty"`

	// List of optional references to patch archives to download
	// and extract at the root of the proxy. Gzippied tarballs only.
	//+optional
	Patches []ResourceRef `json:"patches,omitempty"`

	// Number of maximum players that can connect to the
	// ProxyFleet Deployment.
	//+kubebuilder:default=100
	MaxPlayers int32 `json:"maxPlayers,omitempty"`

	// Message to display when the players query the status
	// of the ProxyFleet Deployment.
	//+kubebuilder:default="A Minecraft Cluster on Shulker"
	Motd string `json:"motd,omitempty"`

	// Server icon image in base64 format.
	//+optional
	ServerIcon string `json:"serverIcon,omitempty"`

	// Whether to enable the PROXY protocol.
	//+kubebuilder:default=false
	ProxyProtocol bool `json:"proxyProtocol,omitempty"`

	// Number of seconds the proxy will live before being
	// drained automatically.
	//+kubebuilder:default=86400
	TimeToLiveSeconds int32 `json:"ttlSeconds,omitempty"`
}

// Overrides for the created Pod of the proxy.
type ProxyFleetPodOverridesSpec struct {
	// Image to use as replacement for the built-in one.
	//+optional
	Image *ImageOverrideSpec `json:"image,omitempty"`

	// Extra environment variables to add to the crated Pod.
	Env []corev1.EnvVar `json:"env,omitempty"`

	// The desired compute resource requirements of the created Pod.
	Resources *corev1.ResourceRequirements `json:"resources,omitempty"`

	// Affinity scheduling rules to be applied on created Pod.
	Affinity *corev1.Affinity `json:"affinity,omitempty"`

	// Name of the ServiceAccount to use.
	ServiceAccountName string `json:"serviceAccountName,omitempty"`
}

type ProxyFleetStatusCondition string

const (
	ProxyFleetAvailableCondition ProxyFleetStatusCondition = "Available"
)

// ProxyFleetStatus defines the observed state of ProxyFleet
type ProxyFleetStatus struct {
	// Conditions represent the latest available observations of a
	// ProxyFleet object.
	// Known .status.conditions.type are: "Ready", "Phase".
	//+kubebuilder:validation:Required
	Conditions []metav1.Condition `json:"conditions" patchStrategy:"merge" patchMergeKey:"type" protobuf:"bytes,1,rep,name=conditions"`

	// Number of total replicas in this ProxyFleet.
	Replicas int32 `json:"replicas"`

	// Number of available replicas in this ProxyFleet.
	ReadyReplicas int32 `json:"readyReplicas"`

	// Number of unavailable replicas in this ProxyFleet.
	AllocatedReplicas int32 `json:"allocatedReplicas"`
}

func (s *ProxyFleetStatus) SetCondition(condition ProxyFleetStatusCondition, status metav1.ConditionStatus, reason string, message string) metav1.Condition {
	c := metav1.Condition{
		Type:    string(condition),
		Status:  status,
		Reason:  reason,
		Message: message,
	}

	meta.SetStatusCondition(&s.Conditions, c)
	return c
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status
//+kubebuilder:printcolumn:name="Replicas",type="integer",JSONPath=".status.replicas"
//+kubebuilder:printcolumn:name="Available",type="integer",JSONPath=".status.availableReplicas"
//+kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"
//+kubebuilder:resource:shortName={"skrpf"},categories=all

// ProxyFleet is the Schema for the proxies API
type ProxyFleet struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	ProxyFleetTemplate `json:",inline"`
	Status             ProxyFleetStatus `json:"status,omitempty"`
}

// Template containing the metadata and spec of the
// ProxyFleet. Will be used in ProxyFleetDeployment.
type ProxyFleetTemplate struct {
	//+kubebuilder:validation:Required
	metav1.ObjectMeta `json:"metadata,omitempty"`

	//+kubebuilder:validation:Required
	Spec ProxyFleetSpec `json:"spec"`
}

//+kubebuilder:object:root=true

// ProxyFleetList contains a list of ProxyFleet
type ProxyFleetList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []ProxyFleet `json:"items"`
}

func init() {
	SchemeBuilder.Register(&ProxyFleet{}, &ProxyFleetList{})
}

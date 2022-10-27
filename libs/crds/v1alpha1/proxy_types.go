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

const ProxyDrainAnnotationName = "proxy.shulkermc.io/drain"

// ProxySpec defines the desired state of Proxy
type ProxySpec struct {
	// Reference to a MinecraftCluster. Adding this will enroll
	// this Proxy to be part of a MinecraftCluster.
	//+kubebuilder:validation:Required
	ClusterRef MinecraftClusterRef `json:"clusterRef,omitempty"`

	// Defines the version of the proxy to run.
	// The version can come from a channel which allows the user
	// to run a version different from the default BungeeCord.
	//+kubebuilder:validation:Required
	Version ProxyVersionSpec `json:"version,omitempty"`

	// Custom configuration flags to custom the proxy behavior.
	Configuration ProxyConfigurationSpec `json:"config,omitempty"`

	// Overrides for values to be injected in the created Pod
	// of this Proxy.
	PodOverrides *ProxyPodOverridesSpec `json:"podOverrides,omitempty"`
}

// +kubebuilder:validation:Enum=BungeeCord;Waterfall;Velocity
type ProxyVersionChannel string

const (
	ProxyVersionBungeeCord ProxyVersionChannel = "BungeeCord"
	ProxyVersionWaterfall  ProxyVersionChannel = "Waterfall"
	ProxyVersionVelocity   ProxyVersionChannel = "Velocity"
)

// Defines the version of the proxy to run.
type ProxyVersionSpec struct {
	// Channel of the version to use. Defaults to BungeeCord.
	//+optional
	//+kubebuilder:default=Velocity
	Channel ProxyVersionChannel `json:"channel,omitempty"`

	// Name of the version to use.
	//+kubebuilder:validation:Required
	Name string `json:"name"`
}

type ProxyConfigurationSpec struct {
	// Name of an optional ConfigMap already containing the proxy
	// configuration.
	//+optional
	ExistingConfigMapName string `json:"existingConfigMapName,omitempty"`

	// Number of maximum players that can connect to the
	// Proxy Deployment.
	//+kubebuilder:default=100
	MaxPlayers int32 `json:"maxPlayers,omitempty"`

	// Message to display when the players query the status
	// of the Proxy Deployment.
	//+kubebuilder:default="A Minecraft Cluster on Shulker"
	Motd string `json:"motd,omitempty"`

	// Server icon image in base64 format.
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
type ProxyPodOverridesSpec struct {
	// The desired compute resource requirements of the created Pod.
	Resources *corev1.ResourceRequirements `json:"resources,omitempty"`

	// Affinity scheduling rules to be applied on created Pod.
	Affinity *corev1.Affinity `json:"affinity,omitempty"`

	// Name of the ServiceAccount to use.
	ServiceAccountName string `json:"serviceAccountName,omitempty"`
}

type ProxyStatusCondition string

const (
	ProxyReadyCondition ProxyStatusCondition = "Ready"
	ProxyPhaseCondition ProxyStatusCondition = "Phase"
)

// ProxyStatus defines the observed state of Proxy
type ProxyStatus struct {
	// Conditions represent the latest available observations of a
	// Proxy object.
	// Known .status.conditions.type are: "Ready", "Phase".
	//+kubebuilder:validation:Required
	Conditions []metav1.Condition `json:"conditions,omitempty" patchStrategy:"merge" patchMergeKey:"type" protobuf:"bytes,1,rep,name=conditions"`
}

func (s *ProxyStatus) SetCondition(condition ProxyStatusCondition, status metav1.ConditionStatus, reason string, message string) metav1.Condition {
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
//+kubebuilder:printcolumn:name="Ready",type="boolean",JSONPath=".status.conditions[?(@.type==\"Ready\")].status"
//+kubebuilder:printcolumn:name="Phase",type="string",JSONPath=".status.conditions[?(@.type==\"Phase\")].reason"
//+kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"
//+kubebuilder:resource:shortName={"skrp"},categories=all

// Proxy is the Schema for the proxies API
type Proxy struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	ProxyTemplate `json:",inline"`
	Status        ProxyStatus `json:"status,omitempty"`
}

// Template containing the metadata and spec of the
// Proxy. Will be used in ProxyDeployment.
type ProxyTemplate struct {
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec ProxySpec `json:"spec,omitempty"`
}

//+kubebuilder:object:root=true

// ProxyList contains a list of Proxy
type ProxyList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []Proxy `json:"items"`
}

func init() {
	SchemeBuilder.Register(&Proxy{}, &ProxyList{})
}

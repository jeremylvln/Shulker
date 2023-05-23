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

// MinecraftServerSpec defines the desired state of MinecraftServer
type MinecraftServerSpec struct {
	// Reference to a MinecraftCluster. Adding this will enroll
	// this MinecraftServer to be part of a MinecraftCluster.
	//+kubebuilder:validation:Required
	ClusterRef MinecraftClusterRef `json:"clusterRef,omitempty"`

	// List of tags identifying this MinecraftServer.
	Tags []string `json:"tags,omitempty"`

	// Defines the version of the server to run.
	// The version can come from a channel which allows the user
	// to run a version different from the default Paper.
	//+kubebuilder:validation:Required
	Version MinecraftServerVersionSpec `json:"version"`

	// Custom configuration flags to custom the server behavior.
	//+kubebuilder:default={}
	Configuration MinecraftServerConfigurationSpec `json:"config,omitempty"`

	// Overrides for values to be injected in the created Pod
	// of this MinecraftServer.
	PodOverrides *MinecraftServerPodOverridesSpec `json:"podOverrides,omitempty"`
}

// +kubebuilder:validation:Enum=Paper;Bukkit;Spigot;Pufferfish;Forge;Fabric;Quilt
type MinecraftServerVersionChannel string

const (
	MinecraftServerVersionPaper      MinecraftServerVersionChannel = "Paper"
	MinecraftServerVersionBukkit     MinecraftServerVersionChannel = "Bukkit"
	MinecraftServerVersionSpigot     MinecraftServerVersionChannel = "Spigot"
	MinecraftServerVersionPufferfish MinecraftServerVersionChannel = "Pufferfish"
	MinecraftServerVersionForge      MinecraftServerVersionChannel = "Forge"
	MinecraftServerVersionFabric     MinecraftServerVersionChannel = "Fabric"
	MinecraftServerVersionQuilt      MinecraftServerVersionChannel = "Quilt"
)

// Defines the version of the server to run.
type MinecraftServerVersionSpec struct {
	// Channel of the version to use. Defaults to Paper.
	//+optional
	//+kubebuilder:default=Paper
	Channel MinecraftServerVersionChannel `json:"channel,omitempty"`

	// Name of the version to use.
	//+kubebuilder:validation:Required
	Name string `json:"name"`
}

// +kubebuilder:validation:Enum=BungeeCord;Velocity
type MincraftServerConfigurationProxyForwardingMode string

const (
	MincraftServerConfigurationProxyForwardingModeBungeeCord MincraftServerConfigurationProxyForwardingMode = "BungeeCord"
	MincraftServerConfigurationProxyForwardingModeVelocity   MincraftServerConfigurationProxyForwardingMode = "Velocity"
)

type MinecraftServerConfigurationSpec struct {
	// Name of an optional ConfigMap already containing the server
	// configuration.
	//+optional
	ExistingConfigMapName string `json:"existingConfigMapName,omitempty"`

	// Reference to a world to download and extract. Gzipped tarball
	// only.
	//+optional
	World *ResourceRef `json:"world,omitempty"`

	// List of references to plugins to download.
	//+optional
	Plugins []ResourceRef `json:"plugins,omitempty"`

	// List of optional references to patch archives to download
	// and extract at the root of the server. Gzippied tarballs only.
	//+optional
	Patches []ResourceRef `json:"patches,omitempty"`

	// Number of maximum players that can connect to the MinecraftServer.
	//+kubebuilder:default=20
	MaxPlayers *int `json:"maxPlayers,omitempty"`

	// Whether to allow the MinecraftServer to generate a Nether world
	// and the players to enter it.
	//+kubebuilder:default=true
	DisableNether bool `json:"disableNether,omitempty"`

	// Whether to allow the MinecraftServer to generate a End world
	// and the players to enter it.
	//+kubebuilder:default=true
	DisableEnd bool `json:"disableEnd,omitempty"`

	// Custom properties to set inside the server.properties fil of
	// the Pod. Note: Shulker may override some values.
	//+optional
	ServerProperties map[string]string `json:"serverProperties,omitempty"`

	// Type of forwarding the proxies are using between themselves and
	// this MinecraftServer.
	//+kubebuilder:default=Velocity
	ProxyForwardingMode MincraftServerConfigurationProxyForwardingMode `json:"proxyForwardingMode,omitempty"`
}

// Overrides for the created Pod of the server.
type MinecraftServerPodOverridesSpec struct {
	// Image to use as replacement for the built-in one.
	//+optional
	Image *ImageOverrideSpec `json:"image,omitempty"`

	// Extra environment variables to add to the crated Pod.
	//+optional
	Env []corev1.EnvVar `json:"env,omitempty"`

	// The desired compute resource requirements of the created Pod.
	//+optional
	Resources *corev1.ResourceRequirements `json:"resources,omitempty"`

	// Affinity scheduling rules to be applied on created Pod.
	//+optional
	Affinity *corev1.Affinity `json:"affinity,omitempty"`

	// Tolerations to be applied on created Pod.
	Tolarations []corev1.Toleration `json:"tolerations,omitempty"`

	// Name of the ServiceAccount to use.
	//+optional
	ServiceAccountName string `json:"serviceAccountName,omitempty"`
}

type MinecraftServerStatusCondition string

const (
	MinecraftServerReadyCondition MinecraftServerStatusCondition = "Ready"
)

// MinecraftServerStatus defines the observed state of MinecraftServer
type MinecraftServerStatus struct {
	// Conditions represent the latest available observations of a
	// MinecraftServer object.
	// Known .status.conditions.type are: "Ready", "Phase".
	//+kubebuilder:validation:Required
	Conditions []metav1.Condition `json:"conditions" patchStrategy:"merge" patchMergeKey:"type" protobuf:"bytes,1,rep,name=conditions"`

	// IP address of the GameServer.
	Address string `json:"address"`

	// Port of the GameServer.
	Port int32 `json:"port"`
}

func (s *MinecraftServerStatus) SetCondition(condition MinecraftServerStatusCondition, status metav1.ConditionStatus, reason string, message string) metav1.Condition {
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
//+kubebuilder:resource:shortName={"skrms"},categories=all

// MinecraftServer is the Schema for the minecraftservers API
type MinecraftServer struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	MinecraftServerTemplate `json:",inline"`
	Status                  MinecraftServerStatus `json:"status,omitempty"`
}

// Template containing the metadata and spec of the
// MinecraftServer. Will be used in MinecraftServer.
type MinecraftServerTemplate struct {
	//+kubebuilder:validation:Required
	metav1.ObjectMeta `json:"metadata,omitempty"`

	//+kubebuilder:validation:Required
	Spec MinecraftServerSpec `json:"spec"`
}

//+kubebuilder:object:root=true

// MinecraftServerList contains a list of MinecraftServer
type MinecraftServerList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []MinecraftServer `json:"items"`
}

func init() {
	SchemeBuilder.Register(&MinecraftServer{}, &MinecraftServerList{})
}

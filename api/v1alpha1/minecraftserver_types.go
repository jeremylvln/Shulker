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
//+kubebuilder:printcolumn:name="Cluster Name",type="string",JSONPath=".spec.minecraftClusterRef.name"
//+kubebuilder:printcolumn:name="Ready",type="boolean",JSONPath=".status.conditions[?(@.type==\"Ready\")].status"
//+kubebuilder:printcolumn:name="Address",type="boolean",JSONPath=".status.address"
//+kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"
//+kubebuilder:resource:shortName={"sms"},categories=all

// MinecraftServer is the Schema for the MinecraftServer API.
type MinecraftServer struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   MinecraftServerSpec   `json:"spec,omitempty"`
	Status MinecraftServerStatus `json:"status,omitempty"`
}

// Defines the defired state of a MinecraftServer. Most, to not
// say all, fields configurable in a Minecraft Server can be
// configured in this CRD.
type MinecraftServerSpec struct {
	// Reference to a Minecraft Cluster. Adding this will enroll
	// this Minecraft Server to be part of a Minecraft Cluster.
	ClusterRef *MinecraftClusterRef `json:"minecraftClusterRef"`

	//+kubebuilder:validation:Required
	Version MinecraftServerVersionSpec `json:"version"`

	// Number of maximum players that can connect to the
	// Minecraft Server.
	//+kubebuilder:default=20
	MaxPlayers *int64 `json:"maxPlayers,omitempty"`

	// Message to display when the players query the status
	// of the Minecraft Server.
	//+kubebuilder:default="A Minecraft Server on Shulker"
	Motd string `json:"motd,omitempty"`

	// Server icon image in base64 format.
	ServerIcon string `json:"serverIcon,omitempty"`

	// List of players to be operators on the Minecraft
	// Server.
	Operators []string `json:"operators,omitempty"`

	// List of players to be whitelisted on the Minecraft
	// Server.
	WhitelistedPlayers []string `json:"whitelistedPlayers,omitempty"`

	// URL to a downloadable world archive.
	//+kubebuilder:default={disableNether: false, disableEnd: false}
	World *MinecraftServerWorldSpec `json:"world,omitempty"`

	// Configuration of Minecraft Server's rcon.
	//+kubebuilder:default={enabled: true}
	Rcon *MinecraftServerRconSpec `json:"rcon,omitempty"`

	// The desired state of the Kubernetes Service to create for the
	// Minecraft Server.
	//+kubebuilder:default={enabled: false}
	Service *MinecraftServerServiceSpec `json:"service,omitempty"`

	// Overrides configuration for the Minecraft Server pod.
	//+kubebuilder:default={livenessProbe: {initialDelaySeconds: 60}, readinessProbe: {initialDelaySeconds: 60}, terminationGracePeriodSeconds: 60}
	PodOverrides *MinecraftServerPodOverridesSpec `json:"podOverrides,omitempty"`

	// The desired compute resource requirements of Pods in the Minecraft
	// Server.
	//+kubebuilder:default={limits: {cpu: "2000m", memory: "4Gi"}, requests: {cpu: "1000m", memory: "2Gi"}}
	Resources *corev1.ResourceRequirements `json:"resources,omitempty"`

	// Affinity scheduling rules to be applied on created Pods.
	Affinity *corev1.Affinity `json:"affinity,omitempty"`
}

//+kubebuilder:validation:Enum=Vanilla;Forge;Fabric;Spigot;Paper;Airplane;Pufferfish;Purpur;Magma;Mohist;Catserver;Canyon;SpongeVanilla;Limbo;Crucible
type MinecraftServerVersionChannel string

const (
	MinecraftServerVersionVanilla       MinecraftServerVersionChannel = "Vanilla"
	MinecraftServerVersionForge         MinecraftServerVersionChannel = "Forge"
	MinecraftServerVersionFabric        MinecraftServerVersionChannel = "Fabric"
	MinecraftServerVersionSpigot        MinecraftServerVersionChannel = "Spigot"
	MinecraftServerVersionPaper         MinecraftServerVersionChannel = "Paper"
	MinecraftServerVersionAirplace      MinecraftServerVersionChannel = "Airplane"
	MinecraftServerVersionPufferfish    MinecraftServerVersionChannel = "Pufferfish"
	MinecraftServerVersionPurpur        MinecraftServerVersionChannel = "Purpur"
	MinecraftServerVersionMagma         MinecraftServerVersionChannel = "Magma"
	MinecraftServerVersionMohist        MinecraftServerVersionChannel = "Mohist"
	MinecraftServerVersionCatserver     MinecraftServerVersionChannel = "Catserver"
	MinecraftServerVersionCanyon        MinecraftServerVersionChannel = "Canyon"
	MinecraftServerVersionSpongeVanilla MinecraftServerVersionChannel = "SpongeVanilla"
	MinecraftServerVersionLimbo         MinecraftServerVersionChannel = "Limbo"
	MinecraftServerVersionCrucible      MinecraftServerVersionChannel = "Crucible"
)

// Defines the version of Minecraft to run on the server.
// The version can come from a channel which allows the user
// to run a version different from Mojang's Vanilla.
type MinecraftServerVersionSpec struct {
	// Channel of the version to use. Defaults to Vanilla meaning
	// that the server will run Mojang's official dedicated server.
	//+optional
	//+kubebuilder:default=Vanilla
	Channel MinecraftServerVersionChannel `json:"channel,omitempty"`

	// Name of the version to use with a leading "v". Example: v1.18.1.
	//+kubebuilder:validation:Required
	Name string `json:"name"`
}

type MinecraftServerWorldSpec struct {
	// URL to a downloable world.
	//+kubebuilder:validation:Required
	Url string `json:"url,omitempty"`

	// Whether to allow the Minecraft Server to generate a Nether world
	// and the players to enter it.
	//+kubebuilder:default=false
	DisableNether bool `json:"disableNether,omitempty"`

	// Whether to allow the Minecraft Server to generate a End world
	// and the players to enter it.
	//+kubebuilder:default=false
	DisableEnd bool `json:"disableEnd,omitempty"`
}

type MinecraftServerRconSpec struct {
	// Whether to enable rcon.
	//+kubebuilder:default=true
	Enabled bool `json:"enabled,omitempty"`

	// Name of a Kubernetes Secret containing a `password` key to use
	// as rcon password. If not provided, a Secret will be created
	// dedicated to this Minecraft Server.
	PasswordSecretName string `json:"passwordSecretName,omitempty"`
}

// Configuration attributes for the Service resource.
type MinecraftServerServiceSpec struct {
	// Whether to create a Service for the Minecraft Server. Defaults
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

	// Wether to expose the rcon port or not to the Service.
	//+kubebuilder:default=false
	ExposesRconPort bool `json:"exposesRconPort,omitempty"`
}

type MinecraftServerPodOverridesSpec struct {
	// Additional environment variables to add to the Minecraft
	// Server.
	Env []corev1.EnvVar `json:"env,omitempty"`

	// Overrides for the liveness probe of the Minecraft Server.
	LivenessProbe *MinecraftServerPodProbeSpec `json:"livenessProbe,omitempty"`

	// Overrides for the readiness probe of the Minecraft Server.
	ReadinessProbe *MinecraftServerPodProbeSpec `json:"readinessProbe,omitempty"`

	// Number of seconds before force killing the pod after a graceful
	// termination request. Defaults to 1 minute.
	//+kubebuilder:default=60
	TerminationGracePeriodSeconds *int64 `json:"terminationGracePeriodSeconds,omitempty"`
}

type MinecraftServerPodProbeSpec struct {
	// Number of seconds before starting to perform the probe. Depending
	// on the server configuration, one can take more time than another to
	// be ready. Defaults to 1 minute.
	//+kubebuilder:default=60
	InitialDelaySeconds int32 `json:"initialDelaySeconds,omitempty"`
}

//+kubebuilder:validation:Enum=Ready;Addressable
type MinecraftServerStatusCondition string

const (
	ServerReadyCondition       MinecraftServerStatusCondition = "Ready"
	ServerAddressableCondition MinecraftServerStatusCondition = "Addressable"
)

// MinecraftServerStatus defines the observed state of MinecraftServer
type MinecraftServerStatus struct {
	// Conditions represent the latest available observations of a
	// MinecraftServer object.
	//+kubebuilder:validation:Required
	Conditions []metav1.Condition `json:"conditions"`

	// Address of the Minecraft Server pod.
	Address string `json:"address,omitempty"`
}

func (s *MinecraftServerStatus) SetCondition(condition MinecraftServerStatusCondition, status metav1.ConditionStatus, reason string, message string) {
	meta.SetStatusCondition(&s.Conditions, metav1.Condition{
		Type:    string(condition),
		Status:  status,
		Reason:  reason,
		Message: message,
	})
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

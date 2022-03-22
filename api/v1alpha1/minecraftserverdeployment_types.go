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
//+kubebuilder:resource:shortName={"smsd"},categories=all

// MinecraftServerDeployment is the Schema for the MinecraftServerDeployment API
type MinecraftServerDeployment struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   MinecraftServerDeploymentSpec   `json:"spec,omitempty"`
	Status MinecraftServerDeploymentStatus `json:"status,omitempty"`
}

// MinecraftServerDeploymentSpec defines the desired state of MinecraftServerDeployment
type MinecraftServerDeploymentSpec struct {
	// Reference to a Minecraft Cluster. Adding this will enroll
	// this Proxy Deployment to be part of a Minecraft Cluster.
	//+kubebuilder:validation:Required
	ClusterRef MinecraftClusterRef `json:"minecraftClusterRef"`

	// List of tags identifying this Minecraft Server.
	Tags []string `json:"tags,omitempty"`

	// Number of replicas to create.
	Replicas int32 `json:"replicas"`

	// Version the Minecraft Server Deployment has to run.
	//+kubebuilder:validation:Required
	Version MinecraftServerDeploymentVersionSpec `json:"version"`

	// Number of maximum players that can connect to the
	// Minecraft Server.
	//+kubebuilder:default=20
	MaxPlayers *int16 `json:"maxPlayers,omitempty"`

	// Message to display when the players query the status
	// of the Minecraft Server.
	//+kubebuilder:default="A Minecraft Server on Shulker"
	Motd string `json:"motd,omitempty"`

	// Server icon image in base64 format.
	ServerIcon string `json:"serverIcon,omitempty"`

	// A list of download URLs for plugins.
	Plugins []string `json:"plugins,omitempty"`

	// URL to a downloadable world archive.
	//+kubebuilder:default={disableNether: false, disableEnd: false}
	World *MinecraftServerDeploymentWorldSpec `json:"world,omitempty"`

	// Configuration of Minecraft Server Deployment's rcon.
	//+kubebuilder:default={enabled: true}
	Rcon *MinecraftServerDeploymentRconSpec `json:"rcon,omitempty"`

	// Map of extra files to create when bootstraping the
	// Minecraft Server Deployment's pod.
	ExtraFiles *map[string]string `json:"extraFiles,omitempty"`

	// Overrides configuration for the Minecraft Server pods.
	//+kubebuilder:default={livenessProbe: {initialDelaySeconds: 60}, readinessProbe: {initialDelaySeconds: 60}, terminationGracePeriodSeconds: 60}
	PodOverrides *MinecraftServerDeploymentPodOverridesSpec `json:"podOverrides,omitempty"`

	// The desired compute resource requirements of Pods in the Minecraft
	// Server.
	//+kubebuilder:default={limits: {cpu: "2000m", memory: "4Gi"}, requests: {cpu: "1000m", memory: "2Gi"}}
	Resources *corev1.ResourceRequirements `json:"resources,omitempty"`

	// Affinity scheduling rules to be applied on created Pods.
	Affinity *corev1.Affinity `json:"affinity,omitempty"`
}

//+kubebuilder:validation:Enum=Vanilla;Forge;Fabric;Spigot;Paper;Airplane;Pufferfish;Purpur;Magma;Mohist;Catserver;Canyon;SpongeVanilla;Limbo;Crucible
type MinecraftServerDeploymentVersionChannel string

const (
	MinecraftServerDeploymentVersionVanilla       MinecraftServerDeploymentVersionChannel = "Vanilla"
	MinecraftServerDeploymentVersionForge         MinecraftServerDeploymentVersionChannel = "Forge"
	MinecraftServerDeploymentVersionFabric        MinecraftServerDeploymentVersionChannel = "Fabric"
	MinecraftServerDeploymentVersionSpigot        MinecraftServerDeploymentVersionChannel = "Spigot"
	MinecraftServerDeploymentVersionPaper         MinecraftServerDeploymentVersionChannel = "Paper"
	MinecraftServerDeploymentVersionAirplace      MinecraftServerDeploymentVersionChannel = "Airplane"
	MinecraftServerDeploymentVersionPufferfish    MinecraftServerDeploymentVersionChannel = "Pufferfish"
	MinecraftServerDeploymentVersionPurpur        MinecraftServerDeploymentVersionChannel = "Purpur"
	MinecraftServerDeploymentVersionMagma         MinecraftServerDeploymentVersionChannel = "Magma"
	MinecraftServerDeploymentVersionMohist        MinecraftServerDeploymentVersionChannel = "Mohist"
	MinecraftServerDeploymentVersionCatserver     MinecraftServerDeploymentVersionChannel = "Catserver"
	MinecraftServerDeploymentVersionCanyon        MinecraftServerDeploymentVersionChannel = "Canyon"
	MinecraftServerDeploymentVersionSpongeVanilla MinecraftServerDeploymentVersionChannel = "SpongeVanilla"
	MinecraftServerDeploymentVersionLimbo         MinecraftServerDeploymentVersionChannel = "Limbo"
	MinecraftServerDeploymentVersionCrucible      MinecraftServerDeploymentVersionChannel = "Crucible"
)

// Defines the version of Minecraft to run on the server.
// The version can come from a channel which allows the user
// to run a version different from Mojang's Vanilla.
type MinecraftServerDeploymentVersionSpec struct {
	// Channel of the version to use. Defaults to Vanilla meaning
	// that the server will run Mojang's official dedicated server.
	//+optional
	//+kubebuilder:default=Vanilla
	Channel MinecraftServerDeploymentVersionChannel `json:"channel,omitempty"`

	// Name of the version to use.
	//+kubebuilder:validation:Required
	Name string `json:"name"`
}

type MinecraftServerDeploymentWorldSpec struct {
	// URL to a downloable world.
	Url string `json:"url,omitempty"`

	// URL to a downloable schematic to use when running a Limbo
	// server.
	SchematicUrl string `json:"schematicUrl,omitempty"`

	// World spawn coordinates of the schematic world when running
	// a Limbo server.
	SchematicWorldSpawn string `json:"schematicWorldSpawn,omitempty"`

	// Whether to allow the Minecraft Server to generate a Nether world
	// and the players to enter it.
	//+kubebuilder:default=false
	DisableNether bool `json:"disableNether,omitempty"`

	// Whether to allow the Minecraft Server to generate a End world
	// and the players to enter it.
	//+kubebuilder:default=false
	DisableEnd bool `json:"disableEnd,omitempty"`
}

type MinecraftServerDeploymentRconSpec struct {
	// Whether to enable rcon.
	//+kubebuilder:default=true
	Enabled bool `json:"enabled,omitempty"`

	// Name of a Kubernetes Secret containing a `password` key to use
	// as rcon password. If not provided, a Secret will be created
	// dedicated to this Minecraft Server.
	PasswordSecretName string `json:"passwordSecretName,omitempty"`
}

type MinecraftServerDeploymentPodOverridesSpec struct {
	// Additional labels to add to the Minecraft Server.
	Labels map[string]string `json:"labels,omitempty"`

	// Additional environment variables to add to the Minecraft
	// Server.
	Env []corev1.EnvVar `json:"env,omitempty"`

	// Overrides for the liveness probe of the Minecraft Server.
	//+kubebuilder:default={initialDelaySeconds: 60}
	LivenessProbe *MinecraftServerDeploymentPodProbeSpec `json:"livenessProbe,omitempty"`

	// Overrides for the readiness probe of the Minecraft Server.
	//+kubebuilder:default={initialDelaySeconds: 60}
	ReadinessProbe *MinecraftServerDeploymentPodProbeSpec `json:"readinessProbe,omitempty"`

	// Number of seconds before force killing the pod after a graceful
	// termination request. Defaults to 1 minute.
	//+kubebuilder:default=60
	TerminationGracePeriodSeconds *int64 `json:"terminationGracePeriodSeconds,omitempty"`
}

type MinecraftServerDeploymentPodProbeSpec struct {
	// Number of seconds before starting to perform the probe. Depending
	// on the server configuration, one can take more time than another to
	// be ready. Defaults to 1 minute.
	//+kubebuilder:default=60
	InitialDelaySeconds int32 `json:"initialDelaySeconds,omitempty"`
}

type MinecraftServerDeploymentStatusCondition string

const (
	MinecraftServerDeploymentReadyCondition       MinecraftServerDeploymentStatusCondition = "Ready"
	MinecraftServerDeploymentAvailableCondition   MinecraftServerDeploymentStatusCondition = "Available"
	MinecraftServerDeploymentProgressingCondition MinecraftServerDeploymentStatusCondition = "Progressing"
)

// MinecraftServerDeploymentStatus defines the observed state of MinecraftServerDeployment
type MinecraftServerDeploymentStatus struct {
	// Conditions represent the latest available observations of a
	// MinecraftServerDeployment object.
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

func (s *MinecraftServerDeploymentStatus) SetCondition(condition MinecraftServerDeploymentStatusCondition, status metav1.ConditionStatus, reason string, message string) {
	meta.SetStatusCondition(&s.Conditions, metav1.Condition{
		Type:    string(condition),
		Status:  status,
		Reason:  reason,
		Message: message,
	})
}

//+kubebuilder:object:root=true

// MinecraftServerDeploymentList contains a list of MinecraftServerDeployment
type MinecraftServerDeploymentList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []MinecraftServerDeployment `json:"items"`
}

func init() {
	SchemeBuilder.Register(&MinecraftServerDeployment{}, &MinecraftServerDeploymentList{})
}

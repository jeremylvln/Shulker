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
	meta "k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status
//+kubebuilder:printcolumn:name="Ready",type="boolean",JSONPath=".status.conditions[?(@.type==\"Ready\")].status"
//+kubebuilder:printcolumn:name="Proxies",type="number",JSONPath=".status.proxies"
//+kubebuilder:printcolumn:name="Servers",type="number",JSONPath=".status.servers"
//+kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"
//+kubebuilder:resource:shortName={"smc"},categories=all

// MinecraftCluster is the Schema for the MinecraftCluster API
type MinecraftCluster struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   MinecraftClusterSpec   `json:"spec,omitempty"`
	Status MinecraftClusterStatus `json:"status,omitempty"`
}

// Defines the defired state of a MinecraftCluster. Most, to not
// say all, fields configurable in a Minecraft Cluster can be
// configured in this CRD.
type MinecraftClusterSpec struct {
}

type MinecraftClusterStatusCondition string

const (
	ClusterReadyCondition MinecraftClusterStatusCondition = "Ready"
)

// MinecraftClusterStatus defines the observed state of MinecraftCluster
type MinecraftClusterStatus struct {
	// Conditions represent the latest available observations of a
	// MinecraftCluster object.
	//+kubebuilder:validation:Required
	Conditions []metav1.Condition `json:"conditions"`

	// Number of proxies.
	Proxies int32 `json:"proxies"`

	// Number of servers inside the server pool.
	Servers int32 `json:"servers"`

	// Pool of Minecraft Servers linked to this Minecraft Cluster.
	ServerPool []MinecraftClusterStatusServerEntry `json:"serverPool"`
}

type MinecraftClusterStatusServerEntry struct {
	// Name of the Minecraft Server.
	//+kubebuilder:validation:Required
	Name string `json:"name,omitempty"`

	// IP of the Minecraft Server.
	//+kubebuilder:validation:Required
	Address string `json:"address,omitempty"`
}

func (s *MinecraftClusterStatus) SetCondition(condition MinecraftClusterStatusCondition, status metav1.ConditionStatus, reason string, message string) {
	meta.SetStatusCondition(&s.Conditions, metav1.Condition{
		Type:    string(condition),
		Status:  status,
		Reason:  reason,
		Message: message,
	})
}

//+kubebuilder:object:root=true

// MinecraftClusterList contains a list of MinecraftCluster
type MinecraftClusterList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []MinecraftCluster `json:"items"`
}

type MinecraftClusterRef struct {
	// Name of the Minecraft Cluster.
	//+kubebuilder:validation:Required
	Name string `json:"name,omitempty"`
}

func init() {
	SchemeBuilder.Register(&MinecraftCluster{}, &MinecraftClusterList{})
}

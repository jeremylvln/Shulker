/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// MinecraftClusterSpec defines the desired state of MinecraftCluster
type MinecraftClusterSpec struct {
}

// MinecraftClusterStatus defines the observed state of MinecraftCluster
type MinecraftClusterStatus struct {
	// Number of proxies.
	Proxies int32 `json:"proxies"`

	// Number of servers.
	Servers int32 `json:"servers"`
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status
//+kubebuilder:printcolumn:name="Proxies",type="number",JSONPath=".status.proxies"
//+kubebuilder:printcolumn:name="Servers",type="number",JSONPath=".status.servers"
//+kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"
//+kubebuilder:resource:shortName={"skrmc"},categories=all

// MinecraftCluster is the Schema for the minecraftclusters API
type MinecraftCluster struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   MinecraftClusterSpec   `json:"spec,omitempty"`
	Status MinecraftClusterStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// MinecraftClusterList contains a list of MinecraftCluster
type MinecraftClusterList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []MinecraftCluster `json:"items"`
}

// MinecraftClusterRef is to be used on resources referencing
// a MinecraftCluster.
type MinecraftClusterRef struct {
	// Name of the MinecraftCluster Kubernetes object owning
	// this resource.
	//+kubebuilder:validation:Required
	Name string `json:"name,omitempty"`
}

func init() {
	SchemeBuilder.Register(&MinecraftCluster{}, &MinecraftClusterList{})
}

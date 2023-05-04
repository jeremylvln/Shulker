/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package v1alpha1

import (
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// MinecraftServerFleetSpec defines the desired state of MinecraftServerFleet
type MinecraftServerFleetSpec struct {
	// Reference to a MinecraftCluster. Adding this will enroll
	// this MinecraftServerFleet to be part of a MinecraftCluster.
	//+kubebuilder:validation:Required
	ClusterRef MinecraftClusterRef `json:"clusterRef"`

	// Number of MinecraftServer replicas to create.
	//+kubebuilder:validation:Required
	Replicas int32 `json:"replicas"`

	// Template defining the content of the created MinecraftServers.
	//+kubebuilder:validation:Required
	Template MinecraftServerTemplate `json:"template"`
}

type MinecraftServerFleetStatusCondition string

const (
	MinecraftServerFleetAvailableCondition MinecraftServerFleetStatusCondition = "Available"
)

// MinecraftServerFleetStatus defines the observed state of MinecraftServerFleet
type MinecraftServerFleetStatus struct {
	// Conditions represent the latest available observations of a
	// MinecraftServerFleet object.
	// Known .status.conditions.type are: "Available".
	//+kubebuilder:validation:Required
	Conditions []metav1.Condition `json:"conditions" patchStrategy:"merge" patchMergeKey:"type" protobuf:"bytes,1,rep,name=conditions"`

	// Number of total replicas in this MinecraftServerFleet.
	Replicas int32 `json:"replicas"`

	// Number of available replicas in this MinecraftServerFleet.
	ReadyReplicas int32 `json:"readyReplicas"`

	// Number of unavailable replicas in this MinecraftServerFleet.
	AllocatedReplicas int32 `json:"allocatedReplicas"`
}

func (s *MinecraftServerFleetStatus) SetCondition(condition MinecraftServerFleetStatusCondition, status metav1.ConditionStatus, reason string, message string) metav1.Condition {
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
//+kubebuilder:resource:shortName={"skrmsf"},categories=all

// MinecraftServerFleet is the Schema for the minecraftserverfleets API
type MinecraftServerFleet struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   MinecraftServerFleetSpec   `json:"spec,omitempty"`
	Status MinecraftServerFleetStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// MinecraftServerFleetList contains a list of MinecraftServerFleet
type MinecraftServerFleetList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []MinecraftServerFleet `json:"items"`
}

func init() {
	SchemeBuilder.Register(&MinecraftServerFleet{}, &MinecraftServerFleetList{})
}

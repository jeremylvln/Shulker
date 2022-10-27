/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package v1alpha1

import (
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

const MinecraftServerDeploymentTemplateHashLabelName = "minecraftserverdeployment.shulkermc.io/template-hash"

// MinecraftServerDeploymentSpec defines the desired state of MinecraftServerDeployment
type MinecraftServerDeploymentSpec struct {
	// Reference to a MinecraftCluster. Adding this will enroll
	// this MinecraftServerDeployment to be part of a MinecraftCluster.
	//+kubebuilder:validation:Required
	ClusterRef MinecraftClusterRef `json:"clusterRef,omitempty"`

	// Number of MinecraftServer replicas to create.
	//+kubebuilder:validation:Required
	Replicas int32 `json:"replicas,omitempty"`

	// Template defining the content of the created MinecraftServers.
	//+kubebuilder:validation:Required
	Template MinecraftServerTemplate `json:"template,omitempty"`
}

type MinecraftServerDeploymentStatusCondition string

const (
	MinecraftServerDeploymentAvailableCondition MinecraftServerDeploymentStatusCondition = "Available"
)

// MinecraftServerDeploymentStatus defines the observed state of MinecraftServerDeployment
type MinecraftServerDeploymentStatus struct {
	// Conditions represent the latest available observations of a
	// MinecraftServerDeployment object.
	// Known .status.conditions.type are: "Available".
	//+kubebuilder:validation:Required
	Conditions []metav1.Condition `json:"conditions,omitempty" patchStrategy:"merge" patchMergeKey:"type" protobuf:"bytes,1,rep,name=conditions"`

	// Number of total replicas in this MinecraftServerDeployment.
	Replicas int32 `json:"replicas"`

	// Number of available replicas in this MinecraftServerDeployment.
	AvailableReplicas int32 `json:"availableReplicas"`

	// Number of unavailable replicas in this MinecraftServerDeployment.
	UnavailableReplicas int32 `json:"unavailableReplicas"`

	// Pod label selector.
	Selector string `json:"selector"`
}

func (s *MinecraftServerDeploymentStatus) SetCondition(condition MinecraftServerDeploymentStatusCondition, status metav1.ConditionStatus, reason string, message string) metav1.Condition {
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
//+kubebuilder:subresource:scale:specpath=.spec.replicas,statuspath=.status.replicas,selectorpath=.status.selector
//+kubebuilder:printcolumn:name="Replicas",type="integer",JSONPath=".status.replicas"
//+kubebuilder:printcolumn:name="Available Replicas",type="integer",JSONPath=".status.availableReplicas"
//+kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"
//+kubebuilder:resource:shortName={"skrmsd"},categories=all

// MinecraftServerDeployment is the Schema for the minecraftserverdeployments API
type MinecraftServerDeployment struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   MinecraftServerDeploymentSpec   `json:"spec,omitempty"`
	Status MinecraftServerDeploymentStatus `json:"status,omitempty"`
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

/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	shulkermciov1alpha1 "github.com/jeremylvln/shulker/packages/shulker-crds/v1alpha1"
)

// MatchmakingProfileSpec defines the desired state of MatchmakingProfile
type MatchmakingProfileSpec struct {
	// Reference to a MinecraftCluster. Adding this will enroll
	// this MatchmakingProfile to be part of a MinecraftCluster.
	//+kubebuilder:validation:Required
	ClusterRef shulkermciov1alpha1.MinecraftClusterRef `json:"clusterRef,omitempty"`
}

//+kubebuilder:object:root=true
//+kubebuilder:printcolumn:name="Age",type="date",JSONPath=".metadata.creationTimestamp"
//+kubebuilder:resource:shortName={"skrmp"},categories=all

// MatchmakingProfile is the Schema for the matchmakingprofiles API
type MatchmakingProfile struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	MatchmakingProfileTemplate `json:",inline"`
}

// Template containing the metadata and spec of the
// MatchmakingProfile. Will be used in MatchmakingProfile.
type MatchmakingProfileTemplate struct {
	//+kubebuilder:validation:Required
	metav1.ObjectMeta `json:"metadata,omitempty"`

	//+kubebuilder:validation:Required
	Spec MatchmakingProfileSpec `json:"spec"`
}

//+kubebuilder:object:root=true

// MatchmakingProfileList contains a list of MatchmakingProfile
type MatchmakingProfileList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []MatchmakingProfile `json:"items"`
}

func init() {
	SchemeBuilder.Register(&MatchmakingProfile{}, &MatchmakingProfileList{})
}

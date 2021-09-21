/*
Copyright 2021.

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
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

type MinecraftServerSpecService struct {
	// Whether to create a Service for the MinecraftServer
	//+kubebuilder:default=false
	Enabled bool `json:"enabled,omitempty"`
	// Type of the Service
	//+kubebuilder:default=ClusterIP
	Type string `json:"type,omitempty"`
	// Annotations to add to the Service
	//+kubebuilder:default={}
	Annotations map[string]string `json:"annotations,omitempty"`
	// Labels to add to the Service
	//+kubebuilder:default={}
	Labels map[string]string `json:"labels,omitempty"`
}

// MinecraftServerSpec defines the desired state of MinecraftServer
type MinecraftServerSpec struct {
	// Name of the Template to use
	TemplateRef string `json:"templateRef,omitempty"`
	// Service of the MinecraftServer, optional
	Service MinecraftServerSpecService `json:"service,omitempty"`
}

// MinecraftServerStatus defines the observed state of MinecraftServer
type MinecraftServerStatus struct {
	// Conditions represent the latest available observations of an object's state
	Conditions []metav1.Condition `json:"conditions,omitempty"`
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status

// MinecraftServer is the Schema for the minecraftservers API
type MinecraftServer struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   MinecraftServerSpec   `json:"spec,omitempty"`
	Status MinecraftServerStatus `json:"status,omitempty"`
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

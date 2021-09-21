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

// +kubebuilder:validation:Enum=Vanilla
type TemplateSpecVersionChannel string

const (
	Vanilla TemplateSpecVersionChannel = "Vanilla"
)

type TemplateSpecVersion struct {
	// Minecraft version as published by Mojang
	Name string `json:"name,omitempty"`

	// Publisher to download the server from. Could be either an official or a third-party
	Channel TemplateSpecVersionChannel `json:"channel,omitempty"`
}

// TemplateSpec defines the desired state of Template
type TemplateSpec struct {
	Version TemplateSpecVersion `json:"version,omitempty"`
	// List of TemplateResource to pack into the template.
	Resources []string `json:"templateResourceRefs,omitempty"`
}

// TemplateStatus defines the observed state of Template
type TemplateStatus struct {
	// Date of the last template packing. If any TemplateResource was modified after this date, the template should be repacked.
	LastPackDate metav1.Time `json:"lastPackDate,omitempty"`
	// Conditions represent the latest available observations of an object's state
	Conditions []metav1.Condition `json:"conditions,omitempty"`
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status
//+kubebuilder:printcolumn:name="Ready",type="boolean",JSONPath=".status.conditions[?(@.type==\"Ready\")].status",description="Whether if the resource is ready"

// Template is the Schema for the templates API
type Template struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   TemplateSpec   `json:"spec,omitempty"`
	Status TemplateStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// TemplateList contains a list of Template
type TemplateList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []Template `json:"items"`
}

func init() {
	SchemeBuilder.Register(&Template{}, &TemplateList{})
}

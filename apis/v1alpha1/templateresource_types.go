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

type TemplateResourceSpecSource struct {
	// Url of the file.
	Url string `json:"url,omitempty"`
}

type TemplateResourceSpecDestination struct {
	// Name of the file.
	FileName string `json:"fileName,omitempty"`
	// Path where to add the resource file inside the template.
	Path string `json:"path,omitempty"`
	// Whether the resource file is an archive and need to be extracted.
	Extract bool `json:"extract,omitempty"`
}

// TemplateResourceSpec defines the desired state of TemplateResource
type TemplateResourceSpec struct {
	// Source of the resource file to download.
	Source TemplateResourceSpecSource `json:"source,omitempty"`
	// Destination of the resource file inside the template.
	Destination TemplateResourceSpecDestination `json:"destination,omitempty"`
}

// TemplateResourceStatus defines the observed state of TemplateResource
type TemplateResourceStatus struct {
	// Hash of the resource. Used to detect resource updates.
	Hash string `json:"hash,omitempty"`
	// Conditions represent the latest available observations of an object's state
	Conditions []metav1.Condition `json:"conditions,omitempty"`
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status
//+kubebuilder:printcolumn:name="Ready",type="boolean",JSONPath=".status.conditions[?(@.type==\"Ready\")].status",description="Whether if the resource is ready"

// TemplateResource is the Schema for the templateresources API
type TemplateResource struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   TemplateResourceSpec   `json:"spec,omitempty"`
	Status TemplateResourceStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// TemplateResourceList contains a list of TemplateResource
type TemplateResourceList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []TemplateResource `json:"items"`
}

func init() {
	SchemeBuilder.Register(&TemplateResource{}, &TemplateResourceList{})
}

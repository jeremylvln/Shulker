package v1alpha1

import (
	corev1 "k8s.io/api/core/v1"
)

type ImageOverrideSpec struct {
	// Complete name of the image, including the repository name
	// and tag.
	//+kubebuilder:validation:Required
	Name string `json:"name,omitempty"`

	// Policy about when to pull the image.
	//+kubebuilder:default=IfNotPresent
	//+kubebuilder:validation:Enum=Always;Never;IfNotPresent
	PullPolicy corev1.PullPolicy `json:"pullPolicy,omitempty"`

	// A list of secrets to use to pull the image.
	//+optional
	PullSecrets []corev1.LocalObjectReference `json:"imagePullSecrets,omitempty"`
}

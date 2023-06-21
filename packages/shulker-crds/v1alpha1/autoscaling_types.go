package v1alpha1

import (
	agonesautoscalingv1 "agones.dev/agones/pkg/apis/autoscaling/v1"
)

type FleetAutoscalingSpec struct {
	// Policy built-in to Agones to apply to the autoscaling
	// target. Mutually exclusive with ShulkerPolicy.
	// +optional
	AgonesPolicy *agonesautoscalingv1.FleetAutoscalerPolicy `json:"agonesPolicy,omitempty"`
}

// // +kubebuilder:validation:Enum=ManualSummon
// type FleetAutoscalingShulkerPolicyType string

// const (
// 	FleetAutoscalingShulkerPolicyManualSummon FleetAutoscalingShulkerPolicyType = "ManualSummon"
// )

// type FleetAutoscalingShulkerPolicySpec struct {
// 	// Name of the policy to apply. Some policies may not be
// 	// implemented depending on the target resource.
// 	Type FleetAutoscalingShulkerPolicyType `json:"type,omitempty"`
// }

package v1alpha1

import (
	agonesautoscalingv1 "agones.dev/agones/pkg/apis/autoscaling/v1"
)

type FleetAutoscalingSpec struct {
	// Policy built-in to Agones to apply to the autoscaling
	// target. Mutually exclusive with ShulkerPolicy.
	// +optional
	AgonesPolicy *agonesautoscalingv1.FleetAutoscalerPolicy `json:"agonesPolicy,omitempty"`

	// Custom policy implemented by Shulker to apply to the
	// autoscaling target. Mutually exclusive with AgonesPolicy.
	// +optional
	ShulkerPolicy *FleetAutoscalingShulkerPolicySpec `json:"shulkerPolicy,omitempty"`
}

// +kubebuilder:validation:Enum=SummoningWebhook
type FleetAutoscalingShulkerPolicyType string

const (
	FleetAutoscalingShulkerPolicySummoningWebhook FleetAutoscalingShulkerPolicyType = "SummoningWebhook"
)

type FleetAutoscalingShulkerPolicySpec struct {
	// Name of the policy to apply.
	Type FleetAutoscalingShulkerPolicyType `json:"type,omitempty"`
}

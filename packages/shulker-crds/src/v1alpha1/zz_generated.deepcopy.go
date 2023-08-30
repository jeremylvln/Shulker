//go:build !ignore_autogenerated
// +build !ignore_autogenerated

/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: AGPL-3.0-or-later
*/

// Code generated by controller-gen. DO NOT EDIT.

package v1alpha1

import (
	"agones.dev/agones/pkg/apis/autoscaling/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	runtime "k8s.io/apimachinery/pkg/runtime"
)

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *FleetAutoscalingSpec) DeepCopyInto(out *FleetAutoscalingSpec) {
	*out = *in
	if in.AgonesPolicy != nil {
		in, out := &in.AgonesPolicy, &out.AgonesPolicy
		*out = new(v1.FleetAutoscalerPolicy)
		(*in).DeepCopyInto(*out)
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new FleetAutoscalingSpec.
func (in *FleetAutoscalingSpec) DeepCopy() *FleetAutoscalingSpec {
	if in == nil {
		return nil
	}
	out := new(FleetAutoscalingSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ImageOverrideSpec) DeepCopyInto(out *ImageOverrideSpec) {
	*out = *in
	if in.PullSecrets != nil {
		in, out := &in.PullSecrets, &out.PullSecrets
		*out = make([]corev1.LocalObjectReference, len(*in))
		copy(*out, *in)
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ImageOverrideSpec.
func (in *ImageOverrideSpec) DeepCopy() *ImageOverrideSpec {
	if in == nil {
		return nil
	}
	out := new(ImageOverrideSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftCluster) DeepCopyInto(out *MinecraftCluster) {
	*out = *in
	out.TypeMeta = in.TypeMeta
	in.ObjectMeta.DeepCopyInto(&out.ObjectMeta)
	out.Spec = in.Spec
	out.Status = in.Status
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftCluster.
func (in *MinecraftCluster) DeepCopy() *MinecraftCluster {
	if in == nil {
		return nil
	}
	out := new(MinecraftCluster)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyObject is an autogenerated deepcopy function, copying the receiver, creating a new runtime.Object.
func (in *MinecraftCluster) DeepCopyObject() runtime.Object {
	if c := in.DeepCopy(); c != nil {
		return c
	}
	return nil
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftClusterList) DeepCopyInto(out *MinecraftClusterList) {
	*out = *in
	out.TypeMeta = in.TypeMeta
	in.ListMeta.DeepCopyInto(&out.ListMeta)
	if in.Items != nil {
		in, out := &in.Items, &out.Items
		*out = make([]MinecraftCluster, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftClusterList.
func (in *MinecraftClusterList) DeepCopy() *MinecraftClusterList {
	if in == nil {
		return nil
	}
	out := new(MinecraftClusterList)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyObject is an autogenerated deepcopy function, copying the receiver, creating a new runtime.Object.
func (in *MinecraftClusterList) DeepCopyObject() runtime.Object {
	if c := in.DeepCopy(); c != nil {
		return c
	}
	return nil
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftClusterRef) DeepCopyInto(out *MinecraftClusterRef) {
	*out = *in
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftClusterRef.
func (in *MinecraftClusterRef) DeepCopy() *MinecraftClusterRef {
	if in == nil {
		return nil
	}
	out := new(MinecraftClusterRef)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftClusterSpec) DeepCopyInto(out *MinecraftClusterSpec) {
	*out = *in
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftClusterSpec.
func (in *MinecraftClusterSpec) DeepCopy() *MinecraftClusterSpec {
	if in == nil {
		return nil
	}
	out := new(MinecraftClusterSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftClusterStatus) DeepCopyInto(out *MinecraftClusterStatus) {
	*out = *in
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftClusterStatus.
func (in *MinecraftClusterStatus) DeepCopy() *MinecraftClusterStatus {
	if in == nil {
		return nil
	}
	out := new(MinecraftClusterStatus)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServer) DeepCopyInto(out *MinecraftServer) {
	*out = *in
	out.TypeMeta = in.TypeMeta
	in.ObjectMeta.DeepCopyInto(&out.ObjectMeta)
	in.MinecraftServerTemplate.DeepCopyInto(&out.MinecraftServerTemplate)
	in.Status.DeepCopyInto(&out.Status)
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServer.
func (in *MinecraftServer) DeepCopy() *MinecraftServer {
	if in == nil {
		return nil
	}
	out := new(MinecraftServer)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyObject is an autogenerated deepcopy function, copying the receiver, creating a new runtime.Object.
func (in *MinecraftServer) DeepCopyObject() runtime.Object {
	if c := in.DeepCopy(); c != nil {
		return c
	}
	return nil
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServerConfigurationSpec) DeepCopyInto(out *MinecraftServerConfigurationSpec) {
	*out = *in
	if in.World != nil {
		in, out := &in.World, &out.World
		*out = new(ResourceRef)
		(*in).DeepCopyInto(*out)
	}
	if in.Plugins != nil {
		in, out := &in.Plugins, &out.Plugins
		*out = make([]ResourceRef, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
	if in.Patches != nil {
		in, out := &in.Patches, &out.Patches
		*out = make([]ResourceRef, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
	if in.MaxPlayers != nil {
		in, out := &in.MaxPlayers, &out.MaxPlayers
		*out = new(int)
		**out = **in
	}
	if in.ServerProperties != nil {
		in, out := &in.ServerProperties, &out.ServerProperties
		*out = make(map[string]string, len(*in))
		for key, val := range *in {
			(*out)[key] = val
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServerConfigurationSpec.
func (in *MinecraftServerConfigurationSpec) DeepCopy() *MinecraftServerConfigurationSpec {
	if in == nil {
		return nil
	}
	out := new(MinecraftServerConfigurationSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServerFleet) DeepCopyInto(out *MinecraftServerFleet) {
	*out = *in
	out.TypeMeta = in.TypeMeta
	in.ObjectMeta.DeepCopyInto(&out.ObjectMeta)
	in.Spec.DeepCopyInto(&out.Spec)
	in.Status.DeepCopyInto(&out.Status)
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServerFleet.
func (in *MinecraftServerFleet) DeepCopy() *MinecraftServerFleet {
	if in == nil {
		return nil
	}
	out := new(MinecraftServerFleet)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyObject is an autogenerated deepcopy function, copying the receiver, creating a new runtime.Object.
func (in *MinecraftServerFleet) DeepCopyObject() runtime.Object {
	if c := in.DeepCopy(); c != nil {
		return c
	}
	return nil
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServerFleetList) DeepCopyInto(out *MinecraftServerFleetList) {
	*out = *in
	out.TypeMeta = in.TypeMeta
	in.ListMeta.DeepCopyInto(&out.ListMeta)
	if in.Items != nil {
		in, out := &in.Items, &out.Items
		*out = make([]MinecraftServerFleet, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServerFleetList.
func (in *MinecraftServerFleetList) DeepCopy() *MinecraftServerFleetList {
	if in == nil {
		return nil
	}
	out := new(MinecraftServerFleetList)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyObject is an autogenerated deepcopy function, copying the receiver, creating a new runtime.Object.
func (in *MinecraftServerFleetList) DeepCopyObject() runtime.Object {
	if c := in.DeepCopy(); c != nil {
		return c
	}
	return nil
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServerFleetSpec) DeepCopyInto(out *MinecraftServerFleetSpec) {
	*out = *in
	out.ClusterRef = in.ClusterRef
	in.Template.DeepCopyInto(&out.Template)
	if in.Autoscaling != nil {
		in, out := &in.Autoscaling, &out.Autoscaling
		*out = new(FleetAutoscalingSpec)
		(*in).DeepCopyInto(*out)
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServerFleetSpec.
func (in *MinecraftServerFleetSpec) DeepCopy() *MinecraftServerFleetSpec {
	if in == nil {
		return nil
	}
	out := new(MinecraftServerFleetSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServerFleetStatus) DeepCopyInto(out *MinecraftServerFleetStatus) {
	*out = *in
	if in.Conditions != nil {
		in, out := &in.Conditions, &out.Conditions
		*out = make([]metav1.Condition, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServerFleetStatus.
func (in *MinecraftServerFleetStatus) DeepCopy() *MinecraftServerFleetStatus {
	if in == nil {
		return nil
	}
	out := new(MinecraftServerFleetStatus)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServerList) DeepCopyInto(out *MinecraftServerList) {
	*out = *in
	out.TypeMeta = in.TypeMeta
	in.ListMeta.DeepCopyInto(&out.ListMeta)
	if in.Items != nil {
		in, out := &in.Items, &out.Items
		*out = make([]MinecraftServer, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServerList.
func (in *MinecraftServerList) DeepCopy() *MinecraftServerList {
	if in == nil {
		return nil
	}
	out := new(MinecraftServerList)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyObject is an autogenerated deepcopy function, copying the receiver, creating a new runtime.Object.
func (in *MinecraftServerList) DeepCopyObject() runtime.Object {
	if c := in.DeepCopy(); c != nil {
		return c
	}
	return nil
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServerPodOverridesSpec) DeepCopyInto(out *MinecraftServerPodOverridesSpec) {
	*out = *in
	if in.Image != nil {
		in, out := &in.Image, &out.Image
		*out = new(ImageOverrideSpec)
		(*in).DeepCopyInto(*out)
	}
	if in.Env != nil {
		in, out := &in.Env, &out.Env
		*out = make([]corev1.EnvVar, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
	if in.Resources != nil {
		in, out := &in.Resources, &out.Resources
		*out = new(corev1.ResourceRequirements)
		(*in).DeepCopyInto(*out)
	}
	if in.Affinity != nil {
		in, out := &in.Affinity, &out.Affinity
		*out = new(corev1.Affinity)
		(*in).DeepCopyInto(*out)
	}
	if in.NodeSelector != nil {
		in, out := &in.NodeSelector, &out.NodeSelector
		*out = make(map[string]string, len(*in))
		for key, val := range *in {
			(*out)[key] = val
		}
	}
	if in.Tolarations != nil {
		in, out := &in.Tolarations, &out.Tolarations
		*out = make([]corev1.Toleration, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServerPodOverridesSpec.
func (in *MinecraftServerPodOverridesSpec) DeepCopy() *MinecraftServerPodOverridesSpec {
	if in == nil {
		return nil
	}
	out := new(MinecraftServerPodOverridesSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServerSpec) DeepCopyInto(out *MinecraftServerSpec) {
	*out = *in
	out.ClusterRef = in.ClusterRef
	if in.Tags != nil {
		in, out := &in.Tags, &out.Tags
		*out = make([]string, len(*in))
		copy(*out, *in)
	}
	out.Version = in.Version
	in.Configuration.DeepCopyInto(&out.Configuration)
	if in.PodOverrides != nil {
		in, out := &in.PodOverrides, &out.PodOverrides
		*out = new(MinecraftServerPodOverridesSpec)
		(*in).DeepCopyInto(*out)
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServerSpec.
func (in *MinecraftServerSpec) DeepCopy() *MinecraftServerSpec {
	if in == nil {
		return nil
	}
	out := new(MinecraftServerSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServerStatus) DeepCopyInto(out *MinecraftServerStatus) {
	*out = *in
	if in.Conditions != nil {
		in, out := &in.Conditions, &out.Conditions
		*out = make([]metav1.Condition, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServerStatus.
func (in *MinecraftServerStatus) DeepCopy() *MinecraftServerStatus {
	if in == nil {
		return nil
	}
	out := new(MinecraftServerStatus)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServerTemplate) DeepCopyInto(out *MinecraftServerTemplate) {
	*out = *in
	in.ObjectMeta.DeepCopyInto(&out.ObjectMeta)
	in.Spec.DeepCopyInto(&out.Spec)
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServerTemplate.
func (in *MinecraftServerTemplate) DeepCopy() *MinecraftServerTemplate {
	if in == nil {
		return nil
	}
	out := new(MinecraftServerTemplate)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *MinecraftServerVersionSpec) DeepCopyInto(out *MinecraftServerVersionSpec) {
	*out = *in
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new MinecraftServerVersionSpec.
func (in *MinecraftServerVersionSpec) DeepCopy() *MinecraftServerVersionSpec {
	if in == nil {
		return nil
	}
	out := new(MinecraftServerVersionSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ProxyConfigurationSpec) DeepCopyInto(out *ProxyConfigurationSpec) {
	*out = *in
	if in.Plugins != nil {
		in, out := &in.Plugins, &out.Plugins
		*out = make([]ResourceRef, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
	if in.Patches != nil {
		in, out := &in.Patches, &out.Patches
		*out = make([]ResourceRef, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ProxyConfigurationSpec.
func (in *ProxyConfigurationSpec) DeepCopy() *ProxyConfigurationSpec {
	if in == nil {
		return nil
	}
	out := new(ProxyConfigurationSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ProxyFleet) DeepCopyInto(out *ProxyFleet) {
	*out = *in
	out.TypeMeta = in.TypeMeta
	in.ObjectMeta.DeepCopyInto(&out.ObjectMeta)
	in.Spec.DeepCopyInto(&out.Spec)
	in.Status.DeepCopyInto(&out.Status)
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ProxyFleet.
func (in *ProxyFleet) DeepCopy() *ProxyFleet {
	if in == nil {
		return nil
	}
	out := new(ProxyFleet)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyObject is an autogenerated deepcopy function, copying the receiver, creating a new runtime.Object.
func (in *ProxyFleet) DeepCopyObject() runtime.Object {
	if c := in.DeepCopy(); c != nil {
		return c
	}
	return nil
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ProxyFleetList) DeepCopyInto(out *ProxyFleetList) {
	*out = *in
	out.TypeMeta = in.TypeMeta
	in.ListMeta.DeepCopyInto(&out.ListMeta)
	if in.Items != nil {
		in, out := &in.Items, &out.Items
		*out = make([]ProxyFleet, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ProxyFleetList.
func (in *ProxyFleetList) DeepCopy() *ProxyFleetList {
	if in == nil {
		return nil
	}
	out := new(ProxyFleetList)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyObject is an autogenerated deepcopy function, copying the receiver, creating a new runtime.Object.
func (in *ProxyFleetList) DeepCopyObject() runtime.Object {
	if c := in.DeepCopy(); c != nil {
		return c
	}
	return nil
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ProxyFleetServiceSpec) DeepCopyInto(out *ProxyFleetServiceSpec) {
	*out = *in
	if in.Annotations != nil {
		in, out := &in.Annotations, &out.Annotations
		*out = make(map[string]string, len(*in))
		for key, val := range *in {
			(*out)[key] = val
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ProxyFleetServiceSpec.
func (in *ProxyFleetServiceSpec) DeepCopy() *ProxyFleetServiceSpec {
	if in == nil {
		return nil
	}
	out := new(ProxyFleetServiceSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ProxyFleetSpec) DeepCopyInto(out *ProxyFleetSpec) {
	*out = *in
	out.ClusterRef = in.ClusterRef
	in.Service.DeepCopyInto(&out.Service)
	in.Template.DeepCopyInto(&out.Template)
	if in.Autoscaling != nil {
		in, out := &in.Autoscaling, &out.Autoscaling
		*out = new(FleetAutoscalingSpec)
		(*in).DeepCopyInto(*out)
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ProxyFleetSpec.
func (in *ProxyFleetSpec) DeepCopy() *ProxyFleetSpec {
	if in == nil {
		return nil
	}
	out := new(ProxyFleetSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ProxyFleetStatus) DeepCopyInto(out *ProxyFleetStatus) {
	*out = *in
	if in.Conditions != nil {
		in, out := &in.Conditions, &out.Conditions
		*out = make([]metav1.Condition, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ProxyFleetStatus.
func (in *ProxyFleetStatus) DeepCopy() *ProxyFleetStatus {
	if in == nil {
		return nil
	}
	out := new(ProxyFleetStatus)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ProxyPodOverridesSpec) DeepCopyInto(out *ProxyPodOverridesSpec) {
	*out = *in
	if in.Image != nil {
		in, out := &in.Image, &out.Image
		*out = new(ImageOverrideSpec)
		(*in).DeepCopyInto(*out)
	}
	if in.Env != nil {
		in, out := &in.Env, &out.Env
		*out = make([]corev1.EnvVar, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
	if in.Resources != nil {
		in, out := &in.Resources, &out.Resources
		*out = new(corev1.ResourceRequirements)
		(*in).DeepCopyInto(*out)
	}
	if in.Affinity != nil {
		in, out := &in.Affinity, &out.Affinity
		*out = new(corev1.Affinity)
		(*in).DeepCopyInto(*out)
	}
	if in.NodeSelector != nil {
		in, out := &in.NodeSelector, &out.NodeSelector
		*out = make(map[string]string, len(*in))
		for key, val := range *in {
			(*out)[key] = val
		}
	}
	if in.Tolarations != nil {
		in, out := &in.Tolarations, &out.Tolarations
		*out = make([]corev1.Toleration, len(*in))
		for i := range *in {
			(*in)[i].DeepCopyInto(&(*out)[i])
		}
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ProxyPodOverridesSpec.
func (in *ProxyPodOverridesSpec) DeepCopy() *ProxyPodOverridesSpec {
	if in == nil {
		return nil
	}
	out := new(ProxyPodOverridesSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ProxySpec) DeepCopyInto(out *ProxySpec) {
	*out = *in
	out.Version = in.Version
	in.Configuration.DeepCopyInto(&out.Configuration)
	if in.PodOverrides != nil {
		in, out := &in.PodOverrides, &out.PodOverrides
		*out = new(ProxyPodOverridesSpec)
		(*in).DeepCopyInto(*out)
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ProxySpec.
func (in *ProxySpec) DeepCopy() *ProxySpec {
	if in == nil {
		return nil
	}
	out := new(ProxySpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ProxyTemplate) DeepCopyInto(out *ProxyTemplate) {
	*out = *in
	in.ObjectMeta.DeepCopyInto(&out.ObjectMeta)
	in.Spec.DeepCopyInto(&out.Spec)
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ProxyTemplate.
func (in *ProxyTemplate) DeepCopy() *ProxyTemplate {
	if in == nil {
		return nil
	}
	out := new(ProxyTemplate)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ProxyVersionSpec) DeepCopyInto(out *ProxyVersionSpec) {
	*out = *in
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ProxyVersionSpec.
func (in *ProxyVersionSpec) DeepCopy() *ProxyVersionSpec {
	if in == nil {
		return nil
	}
	out := new(ProxyVersionSpec)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ResourceRef) DeepCopyInto(out *ResourceRef) {
	*out = *in
	if in.UrlFrom != nil {
		in, out := &in.UrlFrom, &out.UrlFrom
		*out = new(ResourceRefSource)
		(*in).DeepCopyInto(*out)
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ResourceRef.
func (in *ResourceRef) DeepCopy() *ResourceRef {
	if in == nil {
		return nil
	}
	out := new(ResourceRef)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ResourceRefMavenSelector) DeepCopyInto(out *ResourceRefMavenSelector) {
	*out = *in
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ResourceRefMavenSelector.
func (in *ResourceRefMavenSelector) DeepCopy() *ResourceRefMavenSelector {
	if in == nil {
		return nil
	}
	out := new(ResourceRefMavenSelector)
	in.DeepCopyInto(out)
	return out
}

// DeepCopyInto is an autogenerated deepcopy function, copying the receiver, writing into out. in must be non-nil.
func (in *ResourceRefSource) DeepCopyInto(out *ResourceRefSource) {
	*out = *in
	if in.MavenRef != nil {
		in, out := &in.MavenRef, &out.MavenRef
		*out = new(ResourceRefMavenSelector)
		**out = **in
	}
}

// DeepCopy is an autogenerated deepcopy function, copying the receiver, creating a new ResourceRefSource.
func (in *ResourceRefSource) DeepCopy() *ResourceRefSource {
	if in == nil {
		return nil
	}
	out := new(ResourceRefSource)
	in.DeepCopyInto(out)
	return out
}
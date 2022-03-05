/*
Copyright 2022.

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

package controllers

import (
	"context"

	corev1 "k8s.io/api/core/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"

	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
	resource "shulkermc.io/m/v2/internal/resource/server"
)

// MinecraftServerReconciler reconciles a MinecraftServer object
type MinecraftServerReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups="",resources=pods,verbs=get;list;watch;create;update;delete
//+kubebuilder:rbac:groups="",resources=services,verbs=get;list;watch;create;update
//+kubebuilder:rbac:groups="",resources=configmaps,verbs=get;list;watch;create;update
//+kubebuilder:rbac:groups="",resources=secrets,verbs=get;list;watch;create;update
//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftservers,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftservers/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftservers/finalizers,verbs=update

func (r *MinecraftServerReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	logger := log.FromContext(ctx)

	minecraftServer, err := r.getMinecraftServer(ctx, req.NamespacedName)

	if client.IgnoreNotFound(err) != nil {
		return ctrl.Result{}, err
	} else if k8serrors.IsNotFound(err) {
		// No need to requeue if the resource no longer exists
		return ctrl.Result{}, nil
	}

	// Check if the resource has been marked for deletion
	if !minecraftServer.ObjectMeta.DeletionTimestamp.IsZero() {
		logger.Info("Deleting")
		return ctrl.Result{}, r.prepareForDeletion(ctx, minecraftServer)
	}

	if err := r.addFinalizerIfNeeded(ctx, minecraftServer); err != nil {
		return ctrl.Result{}, err
	}

	resourceBuilder := resource.MinecraftServerResourceBuilder{
		Instance: minecraftServer,
		Scheme:   r.Scheme,
	}
	builders, dirtyBuilders := resourceBuilder.ResourceBuilders()

	err = ReconcileWithResourceBuilders(r.Client, ctx, builders, dirtyBuilders)
	if err != nil {
		return ctrl.Result{}, err
	}

	pod := corev1.Pod{}
	err = r.Get(ctx, client.ObjectKey{
		Namespace: minecraftServer.Namespace,
		Name:      resourceBuilder.GetPodName(),
	}, &pod)
	if err != nil {
		return ctrl.Result{}, err
	}

	if pod.Status.PodIP != "" {
		minecraftServer.Status.Address = pod.Status.PodIP
		minecraftServer.Status.SetCondition(shulkermciov1alpha1.ServerAddressableCondition, metav1.ConditionTrue, "PodIPAssigned", "IP assigned to Pod")
	} else {
		minecraftServer.Status.SetCondition(shulkermciov1alpha1.ServerAddressableCondition, metav1.ConditionFalse, "NoPodIPYet", "Pod does not have an IP yet")
	}

	minecraftServer.Status.SetCondition(shulkermciov1alpha1.ServerReadyCondition, metav1.ConditionFalse, "Unknown", "Pod status is unknown")
	for _, condition := range pod.Status.Conditions {
		if condition.Type == corev1.PodReady {
			if condition.Status == corev1.ConditionTrue {
				minecraftServer.Status.SetCondition(shulkermciov1alpha1.ServerReadyCondition, metav1.ConditionTrue, "PodReady", condition.Message)
			} else {
				minecraftServer.Status.SetCondition(shulkermciov1alpha1.ServerReadyCondition, metav1.ConditionFalse, "PodNotReady", condition.Message)
			}

			break
		}
	}

	return ctrl.Result{}, r.Status().Update(ctx, minecraftServer)
}

func (r *MinecraftServerReconciler) getMinecraftServer(ctx context.Context, namespacedName types.NamespacedName) (*shulkermciov1alpha1.MinecraftServer, error) {
	minecraftServerInstance := &shulkermciov1alpha1.MinecraftServer{}
	err := r.Get(ctx, namespacedName, minecraftServerInstance)
	return minecraftServerInstance, err
}

func (r *MinecraftServerReconciler) SetupWithManager(mgr ctrl.Manager) error {
	err := mgr.GetFieldIndexer().IndexField(context.Background(), &shulkermciov1alpha1.MinecraftServer{}, ".spec.minecraftClusterRef.name", func(object client.Object) []string {
		minecraftServer := object.(*shulkermciov1alpha1.MinecraftServer)

		if minecraftServer.Spec.ClusterRef == nil {
			return nil
		}

		return []string{minecraftServer.Spec.ClusterRef.Name}
	})

	if err != nil {
		return err
	}

	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.MinecraftServer{}).
		Owns(&corev1.Pod{}).
		Owns(&corev1.ConfigMap{}).
		Owns(&corev1.Secret{}).
		Owns(&corev1.Service{}).
		Complete(r)
}

/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
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

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
	resources "github.com/iamblueslime/shulker/libs/resources/src/minecraftserver"
)

// MinecraftServerReconciler reconciles a MinecraftServer object
type MinecraftServerReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups="",resources=pods,verbs=get;list;watch;create;update
//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftservers,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftservers/status,verbs=get;update;patch

func (r *MinecraftServerReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	logger := log.FromContext(ctx)

	logger.Info("Reconciling MinecraftServer")
	minecraftServer, err := r.getMinecraftServer(ctx, req.NamespacedName)

	if client.IgnoreNotFound(err) != nil {
		return ctrl.Result{}, err
	} else if k8serrors.IsNotFound(err) {
		// No need to requeue if the resource no longer exists
		return ctrl.Result{}, nil
	}

	cluster := &shulkermciov1alpha1.MinecraftCluster{}
	err = r.Get(ctx, types.NamespacedName{
		Namespace: minecraftServer.Namespace,
		Name:      minecraftServer.Spec.ClusterRef.Name,
	}, cluster)
	if err != nil {
		logger.Error(err, "Referenced MinecraftCluster does not exists")
		return ctrl.Result{}, err
	}

	resourceBuilder := resources.MinecraftServerResourceBuilder{
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
	if err != nil && !k8serrors.IsNotFound(err) {
		return ctrl.Result{}, err
	}

	if pod.DeletionTimestamp != nil || pod.Status.Phase == corev1.PodSucceeded {
		logger.Info("Pod is terminating, deleting MinecraftServer")
		err = r.Delete(ctx, minecraftServer)
		return ctrl.Result{}, err
	}

	minecraftServer.Status.ServerIP = pod.Status.PodIP

	var readyCondition metav1.Condition

	if err == nil {
		readyCondition = minecraftServer.Status.SetCondition(shulkermciov1alpha1.MinecraftServerReadyCondition, metav1.ConditionFalse, "PodNotReady", "Pod is not ready")

		for _, condition := range pod.Status.Conditions {
			if condition.Type == corev1.PodReady && condition.Status == corev1.ConditionTrue {
				readyCondition = minecraftServer.Status.SetCondition(shulkermciov1alpha1.MinecraftServerReadyCondition, metav1.ConditionTrue, "PodReady", "Pod is ready")
			}
		}
	} else {
		readyCondition = minecraftServer.Status.SetCondition(shulkermciov1alpha1.MinecraftServerReadyCondition, metav1.ConditionUnknown, "PodNotExists", "Pod does not exists")
	}

	if readyCondition.Status == metav1.ConditionTrue {
		minecraftServer.Status.SetCondition(shulkermciov1alpha1.MinecraftServerPhaseCondition, metav1.ConditionUnknown, "Running", "MinecraftServer is running")
	} else {
		minecraftServer.Status.SetCondition(shulkermciov1alpha1.MinecraftServerPhaseCondition, metav1.ConditionUnknown, "Unknown", "MinecraftServer status is unknown")
	}

	return ctrl.Result{}, r.Status().Update(ctx, minecraftServer)
}

func (r *MinecraftServerReconciler) getMinecraftServer(ctx context.Context, namespacedName types.NamespacedName) (*shulkermciov1alpha1.MinecraftServer, error) {
	minecraftServer := &shulkermciov1alpha1.MinecraftServer{}
	err := r.Get(ctx, namespacedName, minecraftServer)
	return minecraftServer, err
}

// SetupWithManager sets up the controller with the Manager.
func (r *MinecraftServerReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.MinecraftServer{}).
		Owns(&corev1.Pod{}).
		Owns(&corev1.ConfigMap{}).
		Complete(r)
}

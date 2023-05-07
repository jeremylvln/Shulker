/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package main

import (
	"context"

	corev1 "k8s.io/api/core/v1"
	rbacv1 "k8s.io/api/rbac/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"

	ctrlutil "github.com/jeremylvln/shulker/packages/shulker-controller-utils/src"
	shulkermciov1alpha1 "github.com/jeremylvln/shulker/packages/shulker-crds/v1alpha1"
	resources "github.com/jeremylvln/shulker/packages/shulker-operator/src/resources/minecraftcluster"
)

// MinecraftClusterReconciler reconciles a MinecraftCluster object
type MinecraftClusterReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=shulkermc.io,resources=minecraftclusters,verbs=get;list;watch;create;update;patch;delete

func (r *MinecraftClusterReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	logger := log.FromContext(ctx)

	logger.Info("Reconciling MinecraftCluster")
	cluster, err := r.getMinecraftCluster(ctx, req.NamespacedName)

	if client.IgnoreNotFound(err) != nil {
		return ctrl.Result{}, err
	} else if k8serrors.IsNotFound(err) {
		// No need to requeue if the resource no longer exists
		return ctrl.Result{}, nil
	}

	resourceBuilder := resources.MinecraftClusterResourceBuilder{
		Instance: cluster,
		Scheme:   r.Scheme,
	}
	builders, dirtyBuilders := resourceBuilder.ResourceBuilders()

	err = ctrlutil.ReconcileWithResourceBuilders(r.Client, ctx, builders, dirtyBuilders)
	if err != nil {
		return ctrl.Result{}, err
	}

	return ctrl.Result{}, r.Status().Update(ctx, cluster)
}

func (r *MinecraftClusterReconciler) getMinecraftCluster(ctx context.Context, namespacedName types.NamespacedName) (*shulkermciov1alpha1.MinecraftCluster, error) {
	cluster := &shulkermciov1alpha1.MinecraftCluster{}
	err := r.Get(ctx, namespacedName, cluster)
	return cluster, err
}

// SetupWithManager sets up the controller with the Manager.
func (r *MinecraftClusterReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.MinecraftCluster{}).
		Owns(&corev1.ServiceAccount{}).
		Owns(&rbacv1.Role{}).
		Owns(&rbacv1.RoleBinding{}).
		Complete(r)
}

/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package controllers

import (
	"context"

	corev1 "k8s.io/api/core/v1"
	rbacv1 "k8s.io/api/rbac/v1"
	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/builder"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/handler"
	"sigs.k8s.io/controller-runtime/pkg/log"
	"sigs.k8s.io/controller-runtime/pkg/predicate"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
	"sigs.k8s.io/controller-runtime/pkg/source"

	shulkermciov1alpha1 "github.com/iamblueslime/shulker/libs/crds/v1alpha1"
	resources "github.com/iamblueslime/shulker/libs/resources/src/minecraftcluster"
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

	err = ReconcileWithResourceBuilders(r.Client, ctx, builders, dirtyBuilders)
	if err != nil {
		return ctrl.Result{}, err
	}

	proxyDeploymentList, err := r.listProxyDeployments(ctx, cluster)
	if err != nil {
		return ctrl.Result{}, err
	}
	cluster.Status.Proxies = 0
	for _, proxyDeployment := range proxyDeploymentList.Items {
		cluster.Status.Proxies += proxyDeployment.Status.AvailableReplicas
	}

	return ctrl.Result{}, r.Status().Update(ctx, cluster)
}

func (r *MinecraftClusterReconciler) getMinecraftCluster(ctx context.Context, namespacedName types.NamespacedName) (*shulkermciov1alpha1.MinecraftCluster, error) {
	cluster := &shulkermciov1alpha1.MinecraftCluster{}
	err := r.Get(ctx, namespacedName, cluster)
	return cluster, err
}

func (r *MinecraftClusterReconciler) listProxyDeployments(ctx context.Context, minecraftCluster *shulkermciov1alpha1.MinecraftCluster) (*shulkermciov1alpha1.ProxyDeploymentList, error) {
	list := shulkermciov1alpha1.ProxyDeploymentList{}
	err := r.List(ctx, &list, client.InNamespace(minecraftCluster.Namespace), client.MatchingFields{
		".spec.clusterRef.name": minecraftCluster.Name,
	})

	if err != nil {
		return nil, err
	}

	return &list, nil
}

func (r *MinecraftClusterReconciler) findMinecraftClusterForProxyDeployment(object client.Object) []reconcile.Request {
	proxyDeployment := object.(*shulkermciov1alpha1.ProxyDeployment)

	return []reconcile.Request{{
		NamespacedName: types.NamespacedName{
			Namespace: proxyDeployment.GetNamespace(),
			Name:      proxyDeployment.Spec.ClusterRef.Name,
		},
	}}
}

// SetupWithManager sets up the controller with the Manager.
func (r *MinecraftClusterReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.MinecraftCluster{}).
		Owns(&corev1.ServiceAccount{}).
		Owns(&rbacv1.Role{}).
		Owns(&rbacv1.RoleBinding{}).
		Watches(
			&source.Kind{Type: &shulkermciov1alpha1.ProxyDeployment{}},
			handler.EnqueueRequestsFromMapFunc(r.findMinecraftClusterForProxyDeployment),
			builder.WithPredicates(predicate.ResourceVersionChangedPredicate{}),
		).
		Complete(r)
}

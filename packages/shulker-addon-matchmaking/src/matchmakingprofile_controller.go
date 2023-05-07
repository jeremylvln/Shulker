/*
Copyright (c) Jérémy Levilain
SPDX-License-Identifier: GPL-3.0-or-later
*/

package main

import (
	"context"

	k8serrors "k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"

	resources "github.com/jeremylvln/shulker/packages/shulker-addon-matchmaking/src/resources/matchmakingProfile"
	ctrlutil "github.com/jeremylvln/shulker/packages/shulker-controller-utils/src"
	matchmakingshulkermciov1alpha1 "github.com/jeremylvln/shulker/packages/shulker-crds/matchmaking/v1alpha1"
	shulkermciov1alpha1 "github.com/jeremylvln/shulker/packages/shulker-crds/v1alpha1"
)

// MatchmakingProfileReconciler reconciles a MatchmakingProfile object
type MatchmakingProfileReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=shulkermc.io,resources=matchmakingprofiles,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=shulkermc.io,resources=matchmakingprofiles/status,verbs=get;update;patch

func (r *MatchmakingProfileReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	logger := log.FromContext(ctx)

	logger.Info("Reconciling MatchmakingProfile")
	matchmakingProfile, err := r.getMatchmakingProfile(ctx, req.NamespacedName)

	if client.IgnoreNotFound(err) != nil {
		return ctrl.Result{}, err
	} else if k8serrors.IsNotFound(err) {
		// No need to requeue if the resource no longer exists
		return ctrl.Result{}, nil
	}

	cluster := &shulkermciov1alpha1.MinecraftCluster{}
	err = r.Get(ctx, types.NamespacedName{
		Namespace: matchmakingProfile.Namespace,
		Name:      matchmakingProfile.Spec.ClusterRef.Name,
	}, cluster)
	if err != nil {
		logger.Error(err, "Referenced MinecraftCluster does not exists")
		return ctrl.Result{}, err
	}

	resourceBuilder := resources.MatchmakingProfileResourceBuilder{
		Instance: matchmakingProfile,
		Scheme:   r.Scheme,
		Client:   r.Client,
		Ctx:      ctx,
	}
	builders, dirtyBuilders := resourceBuilder.ResourceBuilders()

	err = ctrlutil.ReconcileWithResourceBuilders(r.Client, ctx, builders, dirtyBuilders)
	if err != nil {
		return ctrl.Result{}, err
	}

	return ctrl.Result{}, r.Status().Update(ctx, matchmakingProfile)
}

func (r *MatchmakingProfileReconciler) getMatchmakingProfile(ctx context.Context, namespacedName types.NamespacedName) (*matchmakingshulkermciov1alpha1.MatchmakingProfile, error) {
	matchmakingProfile := &matchmakingshulkermciov1alpha1.MatchmakingProfile{}
	err := r.Get(ctx, namespacedName, matchmakingProfile)
	return matchmakingProfile, err
}

// SetupWithManager sets up the controller with the Manager.
func (r *MatchmakingProfileReconciler) SetupWithManager(mgr ctrl.Manager) error {
	err := mgr.GetFieldIndexer().IndexField(context.Background(), &matchmakingshulkermciov1alpha1.MatchmakingProfile{}, ".spec.clusterRef.name", func(object client.Object) []string {
		matchmakingProfile := object.(*matchmakingshulkermciov1alpha1.MatchmakingProfile)
		return []string{matchmakingProfile.Spec.ClusterRef.Name}
	})

	if err != nil {
		return err
	}

	return ctrl.NewControllerManagedBy(mgr).
		For(&matchmakingshulkermciov1alpha1.MatchmakingProfile{}).
		Complete(r)
}

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
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	clientretry "k8s.io/client-go/util/retry"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
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
	builders := resourceBuilder.ResourceBuilders()

	for _, builder := range builders {
		resource, err := builder.Build()
		if err != nil {
			return ctrl.Result{}, err
		}

		// var operationResult controllerutil.OperationResult
		err = clientretry.RetryOnConflict(clientretry.DefaultRetry, func() error {
			var apiError error

			if builder.CanBeUpdated() {
				// operationResult, apiError = controllerutil.CreateOrUpdate(ctx, r.Client, resource, func() error {
				_, apiError = controllerutil.CreateOrUpdate(ctx, r.Client, resource, func() error {
					return builder.Update(resource)
				})
			} else {
				existingResource := resource
				apiError = r.Get(ctx, types.NamespacedName{
					Namespace: resource.GetNamespace(),
					Name:      resource.GetName(),
				}, existingResource)

				if k8serrors.IsNotFound(apiError) {
					apiError = builder.Update(resource)
					if apiError != nil {
						return apiError
					}

					return r.Create(ctx, resource)
				}
			}

			return apiError
		})

		if err != nil {
			// r.setReconcileSuccess(ctx, rabbitmqCluster, corev1.ConditionFalse, "Error", err.Error())
			return ctrl.Result{}, err
		}

		// if err = r.annotateIfNeeded(ctx, logger, builder, operationResult, rabbitmqCluster); err != nil {
		// 	return ctrl.Result{}, err
		// }
	}

	return ctrl.Result{}, nil
}

func (r *MinecraftServerReconciler) getMinecraftServer(ctx context.Context, namespacedName types.NamespacedName) (*shulkermciov1alpha1.MinecraftServer, error) {
	minecraftServerInstance := &shulkermciov1alpha1.MinecraftServer{}
	err := r.Get(ctx, namespacedName, minecraftServerInstance)
	return minecraftServerInstance, err
}

func (r *MinecraftServerReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&shulkermciov1alpha1.MinecraftServer{}).
		Owns(&corev1.Pod{}).
		Owns(&corev1.ConfigMap{}).
		Owns(&corev1.Service{}).
		Complete(r)
}

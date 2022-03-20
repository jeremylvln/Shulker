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

	clientretry "k8s.io/client-go/util/retry"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"

	shulkermciov1alpha1 "shulkermc.io/m/v2/api/v1alpha1"
)

const minecraftServerDeploymentDeletionFinalizer = "deletion.finalizers.proxydeployments.shulkermc.io"

func (r *MinecraftServerDeploymentReconciler) addFinalizerIfNeeded(ctx context.Context, minecraftServerDeployment *shulkermciov1alpha1.MinecraftServerDeployment) error {
	if minecraftServerDeployment.ObjectMeta.DeletionTimestamp.IsZero() && !controllerutil.ContainsFinalizer(minecraftServerDeployment, minecraftServerDeploymentDeletionFinalizer) {
		controllerutil.AddFinalizer(minecraftServerDeployment, minecraftServerDeploymentDeletionFinalizer)

		if err := r.Client.Update(ctx, minecraftServerDeployment); err != nil {
			return err
		}
	}

	return nil
}

func (r *MinecraftServerDeploymentReconciler) removeFinalizer(ctx context.Context, minecraftServerDeployment *shulkermciov1alpha1.MinecraftServerDeployment) error {
	controllerutil.RemoveFinalizer(minecraftServerDeployment, minecraftServerDeploymentDeletionFinalizer)

	if err := r.Client.Update(ctx, minecraftServerDeployment); err != nil {
		return err
	}

	return nil
}

func (r *MinecraftServerDeploymentReconciler) prepareForDeletion(ctx context.Context, minecraftServerDeployment *shulkermciov1alpha1.MinecraftServerDeployment) error {
	if controllerutil.ContainsFinalizer(minecraftServerDeployment, minecraftServerDeploymentDeletionFinalizer) {
		if err := clientretry.RetryOnConflict(clientretry.DefaultRetry, func() error {
			// Custom logic here
			return nil
		}); err != nil {
			ctrl.LoggerFrom(ctx).Error(err, "MinecraftServerDeployment deletion")
		}

		if err := r.removeFinalizer(ctx, minecraftServerDeployment); err != nil {
			ctrl.LoggerFrom(ctx).Error(err, "Failed to remove finalizer for deletion")
			return err
		}
	}
	return nil
}
